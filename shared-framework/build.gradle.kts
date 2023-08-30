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

plugins {
    java
    `java-library`
    checkstyle
    id("com.github.jk1.dependency-license-report") version "2.1"
    id("de.thetaphi.forbiddenapis") version "3.4"
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // Compile and test with the last 7.x version to make sure transition scenarios where
    // the Java API client coexists with a 7.x HLRC work fine
    val elasticsearchVersion = "7.17.7"
    val jacksonVersion = "2.13.3"
    val openTelemetryVersion = "1.29.0"

    // Apache 2.0
    // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-low.html
    api("org.elasticsearch.client", "elasticsearch-rest-client", elasticsearchVersion)

    // Apache 2.0
    // https://search.maven.org/artifact/com.google.code.findbugs/jsr305
    api("com.google.code.findbugs:jsr305:3.0.2")

    // EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
    // https://github.com/eclipse-ee4j/jsonp
    api("jakarta.json:jakarta.json-api:2.0.1")

    // Needed even if using Jackson to have an implementation of the Jsonp object model
    // EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
    // https://github.com/eclipse-ee4j/parsson
    api("org.eclipse.parsson:parsson:1.0.0")

    // OpenTelemetry API for native instrumentation of the client.
    // Apache 2.0
    // https://github.com/open-telemetry/opentelemetry-java
    api("io.opentelemetry", "opentelemetry-api", openTelemetryVersion)

    // EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
    // https://github.com/eclipse-ee4j/jsonb-api
    compileOnly("jakarta.json.bind", "jakarta.json.bind-api", "2.0.0")
    testImplementation("jakarta.json.bind", "jakarta.json.bind-api", "2.0.0")

    // Apache 2.0
    // https://github.com/FasterXML/jackson
    compileOnly("com.fasterxml.jackson.core", "jackson-core", jacksonVersion)
    compileOnly("com.fasterxml.jackson.core", "jackson-databind", jacksonVersion)
    testImplementation("com.fasterxml.jackson.core", "jackson-core", jacksonVersion)
    testImplementation("com.fasterxml.jackson.core", "jackson-databind", jacksonVersion)

    // EPL-2.0 OR BSD-3-Clause
    // https://eclipse-ee4j.github.io/yasson/
    testImplementation("org.eclipse", "yasson", "2.0.4") {
        // Exclude Glassfish as we use Parsson (basically Glassfish renamed in the Jakarta namespace).
        exclude(group = "org.glassfish", module = "jakarta.json")
    }

    // Apache-2.0
    testImplementation("commons-io:commons-io:2.11.0")

    // EPL-2.0
    // https://junit.org/junit5/
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    // MIT
    // https://github.com/classgraph/classgraph
    testImplementation("io.github.classgraph:classgraph:4.8.147")

    // MIT
    // https://www.testcontainers.org/
    testImplementation("org.testcontainers", "testcontainers", "1.17.3")
    testImplementation("org.testcontainers", "elasticsearch", "1.17.3")


    testImplementation("io.opentelemetry", "opentelemetry-sdk", openTelemetryVersion)
    // Use it once it's stable (see Instrumentation.java). Limited to tests for now.
    testImplementation("io.opentelemetry", "opentelemetry-semconv", "$openTelemetryVersion-alpha")
}
