/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

rootProject.name = "elasticsearch-java"

// Include as subprojects all subdirectories that have a "build.gradle.kts" and no ".gradle-standalone"
findSubProjects(rootProject.projectDir, "", "")

fun findSubProjects(parentDir: File, parentName: String, sep: String) {
    (parentDir.listFiles() ?: arrayOf<File>()).
        filter { it.name != "buildSrc" }.
        forEach { dir ->
            if (File(dir, "build.gradle.kts").exists()) {
                if (!File(dir, ".gradle-standalone").exists()) {
                    val childName = parentName + sep + dir.name
                    //println("Adding project '$childName' at $dir")
                    include(childName)
                    findSubProjects(dir, childName, ":")
                }
            }
        }
}
