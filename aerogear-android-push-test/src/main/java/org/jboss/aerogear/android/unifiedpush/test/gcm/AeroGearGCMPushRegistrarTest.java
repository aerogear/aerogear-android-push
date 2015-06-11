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
package org.jboss.aerogear.android.unifiedpush.test.gcm;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.http.HttpException;
import org.jboss.aerogear.android.pipe.http.HttpProvider;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushConfiguration;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushRegistrar;
import org.jboss.aerogear.android.unifiedpush.gcm.UnifiedPushConfig;
import org.jboss.aerogear.android.unifiedpush.test.MainActivity;
import org.jboss.aerogear.android.unifiedpush.test.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.unifiedpush.test.util.UnitTestUtils;
import org.jboss.aerogear.android.unifiedpush.test.util.VoidCallback;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AeroGearGCMPushRegistrarTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    private static final String TEST_SENDER_ID = "272275396485";
    private static final String TEST_SENDER_PASSWORD = "Password";
    private static final String TEST_SENDER_VARIANT = "Variant";
    private static final String TAG = AeroGearGCMPushRegistrarTest.class.getSimpleName();

    public AeroGearGCMPushRegistrarTest() {
        super(MainActivity.class);
    }

    public void testAsRegistrarFailsOnNullSenderId() throws URISyntaxException {
        try {
            AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                    .setPushServerURI(new URI("https://testuri"));

            config.asRegistrar();

        } catch (IllegalStateException ex) {
            assertEquals("SenderIds can't be null or empty", ex.getMessage());
            return; // pass
        }
        fail();
    }

    public void testAsRegistrarFailsOnNullPushServerURI() {
        try {
            AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                    .addSenderId(TEST_SENDER_ID);

            config.asRegistrar();

        } catch (IllegalStateException ex) {
            assertEquals("PushServerURI can't be null", ex.getMessage());
            return; // pass
        }
        fail();
    }

    public void testRegister() throws Exception {
        AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                .addSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearGCMPushRegistrar registrar = (AeroGearGCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        StubHttpProvider provider = new StubHttpProvider();
        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);
        VoidCallback callback = new VoidCallback(latch);

        registrar.register(super.getActivity(), callback);
        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("Latch wasn't called");
        }

        if (callback.exception != null) {
            Log.e(TAG, callback.exception.getMessage(), callback.exception);
            fail(callback.exception.getMessage());
        }

        ArgumentCaptor<String> postCaptore = ArgumentCaptor.forClass(String.class);
        Mockito.verify(provider.mock).post(postCaptore.capture());
        JSONObject object = new JSONObject(postCaptore.getValue());
        assertEquals(UnitTestUtils.getPrivateField(registrar, "deviceToken"), object.getString("deviceToken"));
    }

    public void testUnregister() throws Exception {
        AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                .addSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearGCMPushRegistrar registrar = (AeroGearGCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        StubHttpProvider provider = new StubHttpProvider();
        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);

        StubGCMProvider gcmProvider = new StubGCMProvider();
        UnitTestUtils.setPrivateField(registrar, "gcmProvider", gcmProvider);

        VoidCallback callback = new VoidCallback(latch);

        AeroGearGCMPushRegistrar spy = Mockito.spy(registrar);
        Mockito.doReturn("tempId").when(spy).getRegistrationId((Context) Mockito.any());

        spy.register(super.getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);

        latch = new CountDownLatch(1);
        callback = new VoidCallback(latch);
        spy.unregister(super.getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);

        if (callback.exception != null) {
            Log.e(TAG, callback.exception.getMessage(), callback.exception);
            fail(callback.exception.getMessage());
        }

        Mockito.verify(gcmProvider.mock).unregister();
        Mockito.verify(provider.mock).delete(Mockito.matches("tempId"));
        assertNull(callback.exception);
        assertEquals("", UnitTestUtils.getPrivateField(registrar, "deviceToken"));
    }

    public void testRegisterExceptionsAreCaught() throws Exception {
        AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                .addSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearGCMPushRegistrar registrar = (AeroGearGCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        VoidCallback callback = new VoidCallback(latch);

        StubGCMProvider gcmProvider = new StubGCMProvider();
        UnitTestUtils.setPrivateField(registrar, "gcmProvider", gcmProvider);

        registrar.register(getActivity(), callback);
        latch.await(2, TimeUnit.SECONDS);
        assertNotNull(callback.exception);
        assertFalse(callback.exception instanceof IOException);
    }

    public void testUnregisterExceptionsAreCaught() throws Exception {
        UnifiedPushConfig config = new UnifiedPushConfig()
                .addSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearGCMPushRegistrar registrar = new AeroGearGCMPushRegistrar(config);
        CountDownLatch latch = new CountDownLatch(1);
        VoidCallback callback = new VoidCallback(latch);

        StubGCMProvider gcmProvider = new StubGCMProvider();
        UnitTestUtils.setPrivateField(registrar, "gcmProvider", gcmProvider);

        registrar.unregister(getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);
        assertNotNull(callback.exception);
        assertFalse(callback.exception instanceof IOException);
    }

    public void testUnregisterTwice() throws Exception {

        AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                .addSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearGCMPushRegistrar registrar = (AeroGearGCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        StubHttpProvider provider = new StubHttpProvider();
        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);

        StubGCMProvider gcmProvider = new StubGCMProvider();
        UnitTestUtils.setPrivateField(registrar, "gcmProvider", gcmProvider);

        VoidCallback callback = new VoidCallback(latch);

        AeroGearGCMPushRegistrar spy = Mockito.spy(registrar);
        Mockito.doReturn("tempId").when(spy).getRegistrationId((Context) Mockito.any());

        spy.register(super.getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);

        latch = new CountDownLatch(2);
        callback = new VoidCallback(latch);
        spy.unregister(super.getActivity(), callback);
        spy.unregister(super.getActivity(), callback);
        latch.await(2, TimeUnit.SECONDS);

        assertNotNull(callback.exception);
        assertTrue(callback.exception instanceof IllegalStateException);

    }

    public void testAeroGearGCMPushConfigurationWithoutVariantID() throws Exception {

        try {

            AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration();
            config.addSenderId(TEST_SENDER_ID)
                    .setPushServerURI(new URI("https://testuri"))
                    .setSenderIds(TEST_SENDER_ID)
                    .setSecret(TEST_SENDER_ID)
                    .asRegistrar();

        } catch (IllegalStateException ex) {
            assertEquals("VariantID can't be null", ex.getMessage());
            return; // pass
        }

        fail();

    }

    public void testAeroGearGCMPushConfigurationWithoutSecret() throws Exception {

        try {

            AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration();
            config.addSenderId(TEST_SENDER_ID)
                    .setPushServerURI(new URI("https://testuri"))
                    .setSenderIds(TEST_SENDER_ID)
                    .setVariantID(TEST_SENDER_VARIANT)
                    .asRegistrar();

        } catch (IllegalStateException ex) {
            assertEquals("Secret can't be null", ex.getMessage());
            return; // pass
        }

        fail();

    }

    private class StubHttpProvider implements Provider<HttpProvider> {

        protected final HttpProvider mock = Mockito.mock(HttpProvider.class);

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
        public HttpProvider get(Object... in) {
            return mock;
        }
    }

    private class BrokenStubHttpProvider implements Provider<HttpProvider> {

        protected final HttpProvider mock = Mockito.mock(HttpProvider.class);

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
        public HttpProvider get(Object... in) {
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
