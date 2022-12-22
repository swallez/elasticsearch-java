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

package co.elastic.clients.elasticsearch._helpers.bulk;

import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;

import java.util.List;

/**
 * A listener that is called by a {@link BulkIngester} to allow monitoring requests sent and their result.
 *
 * @param <Context> application-defined contextual data that can be associated to a bulk operation.
 */
public interface BulkListener<Context> {

    /**
     * Called before a bulk request is sent.
     */
    void beforeBulk(long executionId, BulkRequest request, List<Context> contexts);

    /**
     * Called after a bulk request has been processed. Elasticsearch accepted the request, but {@code response} the response may
     * contain both successful and failure response items.
     */
    void afterBulk(long executionId, BulkRequest request, List<Context> contexts, BulkResponse response);

    /**
     * Called when a bulk request could not be sent to Elasticsearch.
     */
    void afterBulk(long executionId, BulkRequest request, List<Context> contexts, Throwable failure);
}
