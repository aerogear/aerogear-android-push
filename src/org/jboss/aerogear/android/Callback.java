/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android;

import java.io.Serializable;

/**
 * A handler for consuming the data/result of an operation.
 *
 * @param <T> The data type of the operation
 */
public interface Callback<T> extends Serializable {

    /**
     * Called when operation completes with success.
     *
     * @param data The received data of the operation.
     */
    void onSuccess(T data);

    /**
     * Invoked when an operation has failed.
     *
     * @param e The exception to give more insights on why the operation has failed.
     */
    void onFailure(Exception e);
}
