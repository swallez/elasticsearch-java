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

import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.LicenseDataCollector
import com.github.jk1.license.render.ReportRenderer
import groovy.lang.Closure
import java.io.File
import java.io.FileWriter

plugins {
    java
    `java-library`
    checkstyle
    `maven-publish`
    id("com.github.jk1.dependency-license-report") version "2.2"
    id("de.thetaphi.forbiddenapis") version "3.4"
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8

    withJavadocJar()
    withSourcesJar()
}

forbiddenApis {
    signaturesFiles = files(File(rootProject.projectDir, "config/forbidden-apis.txt"))
    suppressAnnotations = setOf("co.elastic.clients.util.AllowForbiddenApis")
}

tasks.getByName<ProcessResources>("processResources") {
    eachFile {
        if (name != "apis.json") {
            // Only process main source-set resources (test files are large)
            expand(
                "version" to version,
                "git_revision" to (if (rootProject.extra.has("gitHashFull")) rootProject.extra["gitHashFull"] else "unknown")
            )
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    project(":shared-framework")
    api(project(":shared-framework"))
    api(project(":shared-helpers")) {
        exclude(module = "java-client")
    }
    api(project.parent!!)
}

tasks.withType<Jar> {

    exclude {
        // Filter out files that we had to duplicate in the framework to avoid circular dependency
        // when splitting the project
        val filePath = it.file.path
        filePath.contains("/shared-framework/") && filePath.contains("/elasticsearch/_types/")
    }

    from (
        project(":shared-framework").sourceSets.main.get().output,
        project(":shared-helpers").sourceSets.main.get().output,
        project.parent!!.sourceSets.main.get().output,
    )

    doFirst {
        if (rootProject.extra.has("gitHashFull")) {
            val jar = this as Jar
            jar.manifest.attributes["X-Git-Revision"] = rootProject.extra["gitHashFull"]
            jar.manifest.attributes["X-Git-Commit-Time"] = rootProject.extra["gitCommitTime"]
        } else {
            throw GradleException("No git information available")
        }
    }

    manifest {
        attributes["Implementation-Title"] = "Elasticsearch Java client"
        attributes["Implementation-Vendor"] = "Elastic"
        attributes["Implementation-URL"] = "https://github.com/elastic/elasticsearch-java/"
        attributes["Build-Date"] = rootProject.extra["buildTime"]
    }

    metaInf {
        from("../LICENSE.txt")
        from("../NOTICE.txt")
    }
}

tasks.withType<Javadoc> {

    // Generate javadocs for the project and common projects
    source = project.sourceSets.main.get().java +
            project(":shared-framework").sourceSets.main.get().java
                .matching { exclude { it.path.contains("/elasticsearch/_types/") } } +
            project(":shared-helpers").sourceSets.main.get().java +
            project.parent!!.sourceSets.main.get().java

    classpath = project.sourceSets.main.get().compileClasspath +
            project(":shared-framework").sourceSets.main.get().compileClasspath +
            project(":shared-helpers").sourceSets.main.get().compileClasspath +
            project.parent!!.sourceSets.main.get().compileClasspath

    val opt = options as StandardJavadocDocletOptions
    // Gradle calls javadoc with a list of file and not a path. This prevents doc-files from being copied.
    // Note that we don't need to list common projects since they don't have doc-files
    opt.addStringOption("sourcepath", project.projectDir.path + "/src/main/java")
    opt.docFilesSubDirs(true)
    opt.addBooleanOption("Xdoclint:-missing", true)

    doLast {
        // Javadoc adds its decoration to html doc files, including quite some JS. This slows down the api spec
        // redirector that doesn't need it. So overwrite the target file with the original one.
        val specFile = "co/elastic/clients/elasticsearch/doc-files/api-spec.html"
        val source = File(project.parent!!.projectDir, "src/main/java/" + specFile)
        val target = File(project.projectDir, "build/docs/javadoc/" + specFile)
        source.copyTo(target, overwrite = true)
    }
}

publishing {
    repositories {
        maven {
            // See https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry
            name = "ESJavaGithubPackages"
            url = uri("https://maven.pkg.github.com/elastic/elasticsearch-java")
            credentials(PasswordCredentials::class)
        }

        maven {
            name = "Build"
            url = uri("${rootProject.buildDir}/repository")
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("Elasticsearch Java API Client")
                artifactId = "elasticsearch-java"
                description.set("Elasticsearch Java API Client")
                url.set("https://github.com/elastic/elasticsearch-java/")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Elastic")
                        url.set("https://www.elastic.co")
                        inceptionYear.set("2020")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/elastic/elasticsearch-java.git")
                    developerConnection.set("scm:git:ssh://git@github.com:elastic/elasticsearch-java.git")
                    url.set("https://github.com/elastic/elasticsearch-java/")
                }

//                withXml {
//                    // Set the version of dependencies of the org.elasticsearch.client group to the one that we are building.
//                    // Since the unified release process releases everything at once, this ensures all published artifacts depend
//                    // on the exact same version. This assumes of course that the binary API and the behavior of these dependencies
//                    // are the same as the one used in the dependency section below.
//                    val xPathFactory = javax.xml.xpath.XPathFactory.newInstance()
//                    val depSelector = xPathFactory.newXPath()
//                        .compile("/project/dependencies/dependency[groupId/text() = 'org.elasticsearch.client']")
//                    val versionSelector = xPathFactory.newXPath().compile("version")
//
//                    var foundVersion = false;
//
//                    val deps = depSelector.evaluate(asElement().ownerDocument, javax.xml.xpath.XPathConstants.NODESET)
//                            as org.w3c.dom.NodeList
//
//                    for (i in 0 until deps.length) {
//                        val dep = deps.item(i)
//                        val version = versionSelector.evaluate(dep, javax.xml.xpath.XPathConstants.NODE) as org.w3c.dom.Element
//                        foundVersion = true;
//                        version.textContent = project.version.toString()
//                    }
//
//                    if (!foundVersion) {
//                        throw GradleException("Could not find a 'org.elasticsearch.client' to update dependency version in the POM.")
//                    }
//                }
            }
        }
    }
}

licenseReport {
    renderers = arrayOf(SpdxReporter(File(rootProject.buildDir, "release/dependencies.csv")))
    excludeGroups = arrayOf("org.elasticsearch.client")
}

class SpdxReporter(val dest: File) : ReportRenderer {
    // License names to their SPDX identifier
    val spdxIds = mapOf(
        "The Apache License, Version 2.0" to "Apache-2.0",
        "Apache License, Version 2.0" to "Apache-2.0",
        "The Apache Software License, Version 2.0" to "Apache-2.0",
        "BSD Zero Clause License" to "0BSD",
        "Eclipse Public License 2.0" to "EPL-2.0",
        "Eclipse Public License v. 2.0" to "EPL-2.0",
        "Eclipse Public License - v 2.0" to "EPL-2.0",
        "GNU General Public License, version 2 with the GNU Classpath Exception" to "GPL-2.0 WITH Classpath-exception-2.0",
        "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0" to "CDDL-1.0"
    )

    private fun quote(str: String): String {
        return if (str.contains(',') || str.contains("\"")) {
            "\"" + str.replace("\"", "\"\"") + "\""
        } else {
            str
        }
    }

    override fun render(data: ProjectData?) {
        dest.parentFile.mkdirs()
        FileWriter(dest).use { out ->
            out.append("name,url,version,revision,license\n")
            data?.allDependencies?.forEach { dep ->

                val depVersion = dep.version
                val depName = dep.group + ":" + dep.name

                 val info = LicenseDataCollector.multiModuleLicenseInfo(dep)
                 val depUrl = info.moduleUrls.first()

                 val licenseIds = info.licenses.mapNotNull { license ->
                     license.name?.let {
                         checkNotNull(spdxIds[it]) { "No SPDX identifier for $license" }
                     }
                 }.toSet()

                 // Combine multiple licenses.
                 // See https://spdx.github.io/spdx-spec/appendix-IV-SPDX-license-expressions/#composite-license-expressions
                 val licenseId = licenseIds.joinToString(" OR ")

                out.append("${quote(depName)},${quote(depUrl)},${quote(depVersion)},,${quote(licenseId)}\n")
            }
        }
    }
}
