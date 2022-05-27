/*
 * Copyright (C) 2021 Vaticle
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.vaticle.typedb.studio.view.output

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vaticle.typedb.common.collection.Either
import com.vaticle.typedb.studio.state.connection.QueryRunner
import com.vaticle.typedb.studio.state.connection.QueryRunner.Response
import com.vaticle.typedb.studio.view.common.component.Tabs
import com.vaticle.typedb.studio.view.common.theme.Color
import com.vaticle.typedb.studio.view.editor.TextEditor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class RunOutputGroup constructor(
    private val runner: QueryRunner,
    textEditorState: TextEditor.State,
    colors: Color.StudioTheme
) {

    private val graphCount = AtomicInteger(0)
    private val tableCount = AtomicInteger(0)
    private val logOutput = LogOutput.State(textEditorState, colors, runner.transactionState)
    internal val outputs: MutableList<RunOutput.State> = mutableStateListOf(logOutput)
    internal var active: RunOutput.State by mutableStateOf(logOutput)
    private val serialOutputFutures = LinkedBlockingQueue<Either<CompletableFuture<() -> Unit>, Done>>()
    private val nonSerialOutputFutures = LinkedBlockingQueue<Either<CompletableFuture<Unit>, Done>>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val futuresLatch = CountDownLatch(2)
    internal val tabsState = Tabs.State<RunOutput.State>(coroutineScope)

    object Done

    companion object {
        private const val CONSUMER_PERIOD_MS = 33 // 30 FPS
    }

    init {
        consumeResponses()
        printSerialOutput()
        concludeNonSerialOutput()
        concludeRunnerIsConsumed()
    }

    internal fun isActive(runOutput: RunOutput.State): Boolean {
        return active == runOutput
    }

    internal fun activate(runOutput: RunOutput.State) {
        active = runOutput
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun concludeRunnerIsConsumed() = coroutineScope.launch {
        futuresLatch.await()
        runner.isConsumed()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun concludeNonSerialOutput() = coroutineScope.launch {
        val futures = mutableListOf<CompletableFuture<Unit>>()
        do {
            val future = nonSerialOutputFutures.take()
            if (future.isFirst) futures += future.first()
        } while(future.isFirst)
        CompletableFuture.allOf(*futures.toTypedArray()).join()
        futuresLatch.countDown()
    }

    private fun collectNonSerial(future: CompletableFuture<Unit>) {
        nonSerialOutputFutures.put(Either.first(future))
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun printSerialOutput() = coroutineScope.launch {
        do {
            val future = serialOutputFutures.take()
            if (future.isFirst) future.first().join().invoke()
        } while (future.isFirst)
        futuresLatch.countDown()
    }

    private fun collectSerial(outputFn: () -> Unit) {
        collectSerial(CompletableFuture.completedFuture(outputFn))
    }

    private fun collectSerial(outputFnFuture: CompletableFuture<() -> Unit>?) {
        serialOutputFutures.put(Either.first(outputFnFuture))
    }

    @OptIn(ExperimentalTime::class)
    @Suppress("BlockingMethodInNonBlockingContext")
    private fun consumeResponses() = coroutineScope.launch {
        do {
            val responses: MutableList<Response> = mutableListOf()
            delay(Duration.Companion.milliseconds(CONSUMER_PERIOD_MS))
            runner.responses.drainTo(responses)
            if (responses.isNotEmpty()) responses.forEach { consumeResponse(it) }
        } while (responses.lastOrNull() != Response.Done)
        serialOutputFutures.put(Either.second(Done))
        nonSerialOutputFutures.put(Either.second(Done))
    }

    private fun consumeResponse(response: Response) {
        when (response) {
            is Response.Message -> collectSerial { logOutput.outputFn(response).invoke() }
            is Response.Numeric -> collectSerial { logOutput.outputFn(response.value).invoke() }
            is Response.Stream<*> -> when (response) {
                is Response.Stream.NumericGroups -> consumeResponseStream(response) { logOutput.outputFn(it) }
                is Response.Stream.ConceptMapGroups -> consumeResponseStream(response) { logOutput.outputFn(it) }
                is Response.Stream.ConceptMaps -> {
                    val table = TableOutput.State(
                        transaction = runner.transactionState, number = tableCount.incrementAndGet()
                    )//TODO: .also { outputs.add(it) }
                    val graph = GraphOutput.State(
                        transactionState = runner.transactionState, number = graphCount.incrementAndGet()
                    ).also { outputs.add(it); activate(it) }
                    consumeResponseStream(response, onCompleted = { graph.onQueryCompleted() }) {
                        collectNonSerial(CompletableFuture.supplyAsync { graph.output(it) })
                        collectSerial(CompletableFuture.supplyAsync { logOutput.outputFn(it) })
                        collectSerial(CompletableFuture.supplyAsync { table.outputFn(it) })
                    }
                }
            }
            is Response.Done -> {}
        }
    }

    private fun <T> consumeResponseStream(
        stream: Response.Stream<T>, onCompleted: (() -> Unit)? = null, output: (T) -> Unit
    ) {
        val responses: MutableList<Either<T, Response.Done>> = mutableListOf()
        do {
            Thread.sleep(CONSUMER_PERIOD_MS.toLong())
            responses.clear()
            stream.queue.drainTo(responses)
            if (responses.isNotEmpty()) responses.filter { it.isFirst }.forEach { output(it.first()) }
        } while (responses.lastOrNull()?.isSecond != true)
        onCompleted?.let { it() }
    }
}
