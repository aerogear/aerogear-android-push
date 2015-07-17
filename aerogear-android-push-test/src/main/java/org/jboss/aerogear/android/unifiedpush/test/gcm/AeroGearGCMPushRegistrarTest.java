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

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import com.google.android.gms.iid.InstanceID;
import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
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
import org.jboss.aerogear.android.unifiedpush.gcm.GCMSharedPreferenceProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class AeroGearGCMPushRegistrarTest extends PatchedActivityInstrumentationTestCase {

    private static final String TEST_SENDER_ID = "272275396485";
    private static final String TEST_REGISTRAR_PREFERENCES_KEY = "org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushRegistrar:272275396485";
    private static final String TEST_SENDER_PASSWORD = "Password";
    private static final String TEST_SENDER_VARIANT = "Variant";
    private static final String TAG = AeroGearGCMPushRegistrarTest.class.getSimpleName();

    public AeroGearGCMPushRegistrarTest() {
        super(MainActivity.class);
    }

    @Test
    public void testAsRegistrarFailsOnNullSenderId() throws URISyntaxException {
        try {
            AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                    .setPushServerURI(new URI("https://testuri"));

            config.asRegistrar();

        } catch (IllegalStateException ex) {
            //TO check the message I need to use a exception Rule which 
            //I am having trouble getting to work right in Android
            Assert.assertEquals("SenderId can't be null or empty", ex.getMessage());
            return; // pass
        }
        Assert.fail();
    }

    @Test
    public void testAsRegistrarFailsOnNullPushServerURI() {
        try {
            AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                    .setSenderId(TEST_SENDER_ID);

            config.asRegistrar();

        } catch (IllegalStateException ex) {
            //TO check the message I need to use a exception Rule which 
            //I am having trouble getting to work right in Android
            Assert.assertEquals("PushServerURI can't be null", ex.getMessage());
            return; // pass
        }
        Assert.fail();
    }

    @Test
    public void testRegister() throws Exception {
        AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                .setSenderId(TEST_SENDER_ID)
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
            Assert.fail("Latch wasn't called");
        }

        if (callback.exception != null) {
            Log.e(TAG, callback.exception.getMessage(), callback.exception);
            Assert.fail(callback.exception.getMessage());
        }

        ArgumentCaptor<String> postCaptore = ArgumentCaptor.forClass(String.class);
        Mockito.verify(provider.mock).post(postCaptore.capture());
        JSONObject object = new JSONObject(postCaptore.getValue());
        Assert.assertEquals(UnitTestUtils.getPrivateField(registrar, "deviceToken"), object.getString("deviceToken"));
        String jsonData = new GCMSharedPreferenceProvider().get(getActivity()).getString(TEST_REGISTRAR_PREFERENCES_KEY, TAG);
        Assert.assertNotNull(jsonData);
        Assert.assertEquals(UnitTestUtils.getPrivateField(registrar, "deviceToken"), new JSONObject(jsonData).getString("deviceToken"));
    }

    @Test
    public void testUnregister() throws Exception {
        AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                .setSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearGCMPushRegistrar registrar = (AeroGearGCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        StubHttpProvider provider = new StubHttpProvider();
        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);

        StubInstanceIDProvider instanceIdProvider = new StubInstanceIDProvider();
        UnitTestUtils.setPrivateField(registrar, "instanceIdProvider", instanceIdProvider);

        VoidCallback callback = new VoidCallback(latch);

        AeroGearGCMPushRegistrar spy = Mockito.spy(registrar);
        
        spy.register(super.getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);

        latch = new CountDownLatch(1);
        callback = new VoidCallback(latch);
        spy.unregister(super.getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);

        if (callback.exception != null) {
            Log.e(TAG, callback.exception.getMessage(), callback.exception);
            Assert.fail(callback.exception.getMessage());
        }

        Mockito.verify(instanceIdProvider.mock).deleteToken(anyString(), anyString());
        Mockito.verify(provider.mock).delete(Mockito.matches("tempId"));
        Assert.assertNull(callback.exception);
        Assert.assertEquals("", UnitTestUtils.getPrivateField(registrar, "deviceToken"));
    }

    @Test
    public void testRegisterExceptionsAreCaught() throws Exception {
        AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                .setSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearGCMPushRegistrar registrar = (AeroGearGCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        VoidCallback callback = new VoidCallback(latch);

        StubInstanceIDProvider instanceIdProvider = new StubInstanceIDProvider();
        UnitTestUtils.setPrivateField(registrar, "instanceIdProvider", instanceIdProvider);

        registrar.register(getActivity(), callback);
        latch.await(2, TimeUnit.SECONDS);
        Assert.assertNotNull(callback.exception);
        Assert.assertFalse(callback.exception instanceof IOException);
    }

    @Test
    public void testUnregisterExceptionsAreCaught() throws Exception {
        UnifiedPushConfig config = new UnifiedPushConfig()
                .setSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearGCMPushRegistrar registrar = new AeroGearGCMPushRegistrar(config);
        CountDownLatch latch = new CountDownLatch(1);
        VoidCallback callback = new VoidCallback(latch);

        StubInstanceIDProvider instanceIdProvider = new StubInstanceIDProvider();
        UnitTestUtils.setPrivateField(registrar, "instanceIdProvider", instanceIdProvider);

        registrar.unregister(getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);
        Assert.assertNotNull(callback.exception);
        Assert.assertFalse(callback.exception instanceof IOException);
    }

    @Test
    public void testUnregisterTwice() throws Exception {

        AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                .setSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearGCMPushRegistrar registrar = (AeroGearGCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        StubHttpProvider provider = new StubHttpProvider();
        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);

        StubInstanceIDProvider instanceIdProvider = new StubInstanceIDProvider();
        UnitTestUtils.setPrivateField(registrar, "instanceIdProvider", instanceIdProvider);

        VoidCallback callback = new VoidCallback(latch);

        AeroGearGCMPushRegistrar spy = Mockito.spy(registrar);
        

        spy.register(super.getActivity(), callback);
        latch.await(1, TimeUnit.SECONDS);

        latch = new CountDownLatch(2);
        callback = new VoidCallback(latch);
        spy.unregister(super.getActivity(), callback);
        spy.unregister(super.getActivity(), callback);
        latch.await(2, TimeUnit.SECONDS);

        Assert.assertNotNull(callback.exception);
        Assert.assertTrue(callback.exception instanceof IllegalStateException);

    }

    @Test
    public void testAeroGearGCMPushConfigurationWithoutVariantID() throws Exception {

        try {

            AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration();
            config.setSenderId(TEST_SENDER_ID)
                    .setPushServerURI(new URI("https://testuri"))
                    .setSenderId(TEST_SENDER_ID)
                    .setSecret(TEST_SENDER_ID)
                    .asRegistrar();

        } catch (IllegalStateException ex) {
            Assert.assertEquals("VariantID can't be null", ex.getMessage());
            return; // pass
        }

        Assert.fail();

    }

    @Test
    public void testAeroGearGCMPushConfigurationWithoutSecret() throws Exception {

        try {

            AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration();
            config.setSenderId(TEST_SENDER_ID)
                    .setPushServerURI(new URI("https://testuri"))
                    .setVariantID(TEST_SENDER_VARIANT)
                    .asRegistrar();

        } catch (IllegalStateException ex) {
            Assert.assertEquals("Secret can't be null", ex.getMessage());
            return; // pass
        }

        Assert.fail();

    }

    static class StubHttpProvider implements Provider<HttpProvider> {

        protected final HttpProvider mock = Mockito.mock(HttpProvider.class);

        public StubHttpProvider() {
            byte[] bytes = {1};
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


    static class StubInstanceIDProvider implements Provider<InstanceID> {

        protected final InstanceID mock = Mockito.mock(InstanceID.class);
        private static final String TEMP_ID = "tempId";

        public StubInstanceIDProvider() {
            try {
                when(mock.getToken(anyString(), anyString())).thenReturn(TEMP_ID);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }

        @Override
        public InstanceID get(Object... in) {
            return mock;
        }
    }

}
