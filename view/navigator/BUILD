#
# Copyright (C) 2021 Vaticle
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_jvm_library")

package(default_visibility = ["//view:__pkg__"])

kt_jvm_library(
    name = "navigator",
    srcs = glob(["*.kt"]),
    kotlin_compiler_plugin = "@org_jetbrains_compose_compiler//file",
    deps = [
        "//view/common:common",
        "//state/common:common",
        "//state/project:project",
        "//state:state",

        # External Vaticle Dependencies
        "@vaticle_typedb_common//:common",

        # External Maven Dependencies
        "@maven//:io_github_microutils_kotlin_logging_jvm",
        "@maven//:org_jetbrains_compose_desktop_desktop_jvm",
        "@maven//:org_jetbrains_compose_foundation_foundation_desktop",
        "@maven//:org_jetbrains_compose_foundation_foundation_layout_desktop",
        "@maven//:org_jetbrains_compose_runtime_runtime_desktop",
        "@maven//:org_jetbrains_compose_ui_ui_desktop",
        "@maven//:org_jetbrains_compose_ui_ui_graphics_desktop",
        "@maven//:org_jetbrains_compose_ui_ui_text_desktop",
        "@maven//:org_jetbrains_compose_ui_ui_unit_desktop",
        "@maven//:org_slf4j_slf4j_api",
    ],
    tags = ["maven_coordinates=com.vaticle.typedb:typedb-studio-view-navigator:{pom_version}"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "agpl",
)
