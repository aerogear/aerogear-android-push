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
package org.jboss.aerogear.android.impl.util;

import java.util.concurrent.CountDownLatch;

import org.jboss.aerogear.android.Callback;

public final class VoidCallback implements Callback {

    public Exception exception;
    private CountDownLatch latch;

    public VoidCallback() {
    }

    public VoidCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(Object data) {
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public void onFailure(Exception e) {
        this.exception = e;
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public int hashCode() {
        return 1;
    }

}
