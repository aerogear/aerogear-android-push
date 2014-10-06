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

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpException;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
import org.jboss.aerogear.android.impl.http.HttpRestProviderForPush;
import org.jboss.aerogear.android.impl.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.impl.util.VoidCallback;
import org.jboss.aerogear.android.unifiedpush.MainActivity;
import org.jboss.aerogear.android.unifiedpush.PushConfig;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class AeroGearGCMPushRegistrarTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    private static final String TEST_SENDER_ID = "272275396485";

    public AeroGearGCMPushRegistrarTest() {
        super(MainActivity.class);
    }

    public void testRegister() throws Exception {
        PushConfig config = new PushConfig(TEST_SENDER_ID);
        config.setPushServerURI(new URI("https://testuri"));
        AeroGearGCMPushRegistrar registrar = new AeroGearGCMPushRegistrar(config);
        CountDownLatch latch = new CountDownLatch(1);
        StubHttpProvider provider = new StubHttpProvider();
        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);
        VoidCallback callback = new VoidCallback(latch);

        registrar = Mockito.spy(registrar);
        Mockito.doReturn("tempId").when(registrar).getRegistrationId((Context) Mockito.any());

        registrar.register(super.getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);
        assertNull(callback.exception);
        ArgumentCaptor<String> postCaptore = ArgumentCaptor.forClass(String.class);
        Mockito.verify(provider.mock).post(postCaptore.capture());
        JSONObject object = new JSONObject(postCaptore.getValue());
        assertEquals(config.getDeviceToken(), object.getString("deviceToken"));
    }

    public void testUnregister() throws Exception {
        PushConfig config = new PushConfig(TEST_SENDER_ID);
        config.setPushServerURI(new URI("https://testuri"));
        AeroGearGCMPushRegistrar registrar = new AeroGearGCMPushRegistrar(config);
        CountDownLatch latch = new CountDownLatch(1);
        StubHttpProvider provider = new StubHttpProvider();
        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);

        StubGCMProvider gcmProvider = new StubGCMProvider();
        UnitTestUtils.setPrivateField(registrar, "gcmProvider", gcmProvider);

        VoidCallback callback = new VoidCallback(latch);

        registrar = Mockito.spy(registrar);
        Mockito.doReturn("tempId").when(registrar).getRegistrationId((Context) Mockito.any());

        registrar.register(super.getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);

        latch = new CountDownLatch(1);
        callback = new VoidCallback(latch);
        registrar.unregister(super.getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);

        assertNull(callback.exception);
        Mockito.verify(gcmProvider.mock).unregister();
        Mockito.verify(provider.mock).delete(Mockito.matches("tempId"));
        assertNull(callback.exception);
        assertEquals("", config.getDeviceToken());
    }

    public void testRegisterExceptionsAreCaught() throws Exception {
        AeroGearGCMPushRegistrar registrar = new AeroGearGCMPushRegistrar(new PushConfig(""));
        CountDownLatch latch = new CountDownLatch(1);
        VoidCallback callback = new VoidCallback(latch);

        StubGCMProvider gcmProvider = new StubGCMProvider();
        UnitTestUtils.setPrivateField(registrar, "gcmProvider", gcmProvider);

        registrar.register(getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);
        assertNotNull(callback.exception);
        assertFalse(callback.exception instanceof IOException);
    }

    public void testUnregisterExceptionsAreCaught() throws Exception {
        AeroGearGCMPushRegistrar registrar = new AeroGearGCMPushRegistrar(new PushConfig(""));
        CountDownLatch latch = new CountDownLatch(1);
        VoidCallback callback = new VoidCallback(latch);

        StubGCMProvider gcmProvider = new StubGCMProvider();
        UnitTestUtils.setPrivateField(registrar, "gcmProvider", gcmProvider);

        registrar.unregister(getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);
        assertNotNull(callback.exception);
        assertFalse(callback.exception instanceof IOException);
    }

    private class StubHttpProvider implements Provider<HttpRestProviderForPush> {

        protected final HttpRestProviderForPush mock = Mockito.mock(HttpRestProviderForPush.class);

        public StubHttpProvider() {
            byte[] bytes = { 1 };
            Mockito.doReturn(new HeaderAndBody(bytes, new HashMap<String, Object>()))
                    .when(mock)
                    .post((String) Mockito.any());

            Mockito.doReturn(new HeaderAndBody(bytes, new HashMap<String, Object>()))
                    .when(mock)
                    .delete((String) Mockito.any());
        }

        @Override
        public HttpRestProviderForPush get(Object... in) {
            return mock;
        }
    }

    private class BrokenStubHttpProvider implements Provider<HttpRestProviderForPush> {

        protected final HttpRestProviderForPush mock = Mockito.mock(HttpRestProviderForPush.class);

        public BrokenStubHttpProvider() {
            byte[] bytes = { 1 };
            Mockito.doThrow(new HttpException(bytes, 401))
                    .when(mock)
                    .post((String) Mockito.any());

            Mockito.doThrow(new HttpException(bytes, 401))
                    .when(mock)
                    .delete((String) Mockito.any());
        }

        @Override
        public HttpRestProviderForPush get(Object... in) {
            return mock;
        }
    }

    private class StubGCMProvider implements Provider<GoogleCloudMessaging> {

        protected final GoogleCloudMessaging mock = Mockito.mock(GoogleCloudMessaging.class);

        public StubGCMProvider() {

        }

        @Override
        public GoogleCloudMessaging get(Object... in) {
            return mock;
        }
    }

}
