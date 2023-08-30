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

package co.elastic.clients.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class JarHellDetector {

    public static void verifyClassUniqueness(Class<?> clazz) {
        verifyResourceUniqueness(clazz.getName(), clazz.getClassLoader());
    }

    public static void verifyResourceUniqueness(String name, ClassLoader classLoader) {
        // With Java9 modules, will work only with exported classes/resources. This is actually
        // what we want, as non-exported classes/resources will not conflict.
        List<URL> list = new ArrayList<>();
        try {
            Enumeration<URL> resources = classLoader.getResources("/" + name.replace('.', '/') + ".class");
            while (resources.hasMoreElements()) {
                list.add(resources.nextElement());
            }
        } catch (IOException ioe) {
            // Ignore
        }

        if (list.size() > 1) {
            StringBuilder sb = new StringBuilder("Several versions of " + name + " were found. Please fix the classpath:\n");
            for (URL url: list) {
                sb.append("    ").append(url.toString()).append("\n");
            }
            throw new RuntimeException(sb.toString());
        }
    }
}
