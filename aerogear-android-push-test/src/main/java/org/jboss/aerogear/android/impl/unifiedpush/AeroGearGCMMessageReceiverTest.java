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
package org.jboss.aerogear.android.impl.unifiedpush;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static junit.framework.Assert.assertEquals;
import org.jboss.aerogear.android.impl.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.unifiedpush.AeroGearGCMMessageReceiver;
import org.jboss.aerogear.android.unifiedpush.test.MainActivity;
import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import org.jboss.aerogear.android.unifiedpush.Registrations;

public class AeroGearGCMMessageReceiverTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    public AeroGearGCMMessageReceiverTest() {
        super(MainActivity.class);
    }

    public void testConsumeMessageNoMetadata() throws InterruptedException {
        Context ctx = getActivity().getApplicationContext();
        AeroGearGCMMessageReceiver receiver = new AeroGearGCMMessageReceiver();
        CountDownLatch latch = new CountDownLatch(1);
        TestMessageHandler handler = new TestMessageHandler(latch);
        Registrations.registerMainThreadHandler(handler);
        IntentFilter filter = new IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        Intent myIntent = new Intent("com.google.android.c2dm.intent.RECEIVE");

        myIntent.putExtra("testKey", "testValue");
        ctx.registerReceiver(receiver, filter);
        ctx.sendBroadcast(myIntent);
        latch.await(1, TimeUnit.SECONDS);
        assertEquals(TestMessageHandler.Result.MESSAGE, handler.resultType);
        assertEquals("testValue", handler.result.getString("testKey"));
    }

    public void testConsumeMessageDelete() throws InterruptedException {
        Context ctx = getActivity().getApplicationContext();
        AeroGearGCMMessageReceiver receiver = new AeroGearGCMMessageReceiver();
        CountDownLatch latch = new CountDownLatch(1);
        TestMessageHandler handler = new TestMessageHandler(latch);
        Registrations.registerMainThreadHandler(handler);
        IntentFilter filter = new IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        Intent myIntent = new Intent("com.google.android.c2dm.intent.RECEIVE").putExtra("message_type", "deleted_messages");

        ctx.registerReceiver(receiver, filter);
        ctx.sendBroadcast(myIntent);
        latch.await(1, TimeUnit.SECONDS);
        assertEquals(TestMessageHandler.Result.DELETE, handler.resultType);
        assertEquals(myIntent.getExtras().getString("message_type"), handler.result.getString("message_type"));

    }

    public void testConsumeMessageError() throws InterruptedException {
        Context ctx = getActivity().getApplicationContext();
        AeroGearGCMMessageReceiver receiver = new AeroGearGCMMessageReceiver();
        CountDownLatch latch = new CountDownLatch(1);
        TestMessageHandler handler = new TestMessageHandler(latch);
        Registrations.registerMainThreadHandler(handler);
        IntentFilter filter = new IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        Intent myIntent = new Intent("com.google.android.c2dm.intent.RECEIVE").putExtra("message_type", "send_error");

        ctx.registerReceiver(receiver, filter);
        ctx.sendBroadcast(myIntent);
        latch.await(1, TimeUnit.SECONDS);
        assertEquals(TestMessageHandler.Result.ERROR, handler.resultType);
        assertEquals(null, handler.result);

    }

    private static final class TestMessageHandler implements MessageHandler {

        final CountDownLatch latch;
        Bundle result;
        Result resultType;

        enum Result {
            DELETE, MESSAGE, ERROR
        };

        public TestMessageHandler(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onDeleteMessage(Context context, Bundle message) {
            resultType = Result.DELETE;
            result = message;
            latch.countDown();
        }

        @Override
        public void onMessage(Context context, Bundle message) {
            resultType = Result.MESSAGE;
            result = message;
            latch.countDown();
        }

        @Override
        public void onError() {
            resultType = Result.ERROR;
            latch.countDown();
        }

    }

}
