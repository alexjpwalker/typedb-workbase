package com.vaticle.typedb.studio.data

import com.vaticle.typedb.client.TypeDB
import com.vaticle.typedb.client.api.connection.TypeDBClient

class DBServer(val serverAddress: String) {

    val client: TypeDBClient = TypeDB.coreClient(serverAddress)

    fun close() {
        client.close()
    }
}
