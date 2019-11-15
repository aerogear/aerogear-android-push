/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.android.unifiedpush.test.fcm;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.http.HttpProvider;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushConfiguration;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushRegistrar;
import org.jboss.aerogear.android.unifiedpush.fcm.FCMSharedPreferenceProvider;
import org.jboss.aerogear.android.unifiedpush.fcm.UnifiedPushConfig;
import org.jboss.aerogear.android.unifiedpush.test.util.UnitTestUtils;
import org.jboss.aerogear.android.unifiedpush.test.util.VoidCallback;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.InstrumentationRegistry.getContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class AeroGearFCMPushRegistrarTest {

    private static final String TEST_SENDER_ID = "272275396485";
    private static final String TEST_REGISTRAR_PREFERENCES_KEY = "org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushRegistrar:272275396485";
    private static final String TEST_SENDER_PASSWORD = "Password";
    private static final String TEST_SENDER_VARIANT = "Variant";
    private static final String[] CATEGORIES = {"test", "anotherTest"};

    private static final String TAG = AeroGearFCMPushRegistrarTest.class.getSimpleName();

    @Test
    public void testAsRegistrarFailsOnNullSenderId() throws URISyntaxException {
        try {
            AeroGearFCMPushConfiguration config = new AeroGearFCMPushConfiguration()
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
            AeroGearFCMPushConfiguration config = new AeroGearFCMPushConfiguration()
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
        AeroGearFCMPushConfiguration config = new AeroGearFCMPushConfiguration()
                .setSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setCategories(CATEGORIES)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearFCMPushRegistrar registrar = (AeroGearFCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        StubHttpProvider provider = new StubHttpProvider();
        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);

        StubInstanceIDProvider firebaseInstanceIdProvider = new StubInstanceIDProvider();
        UnitTestUtils.setPrivateField(registrar, "firebaseInstanceIdProvider", firebaseInstanceIdProvider);

        VoidCallback callback = new VoidCallback(latch);

        final FirebaseMessaging mockPubSub = mock(FirebaseMessaging.class);
        Mockito.doReturn((Task<Void>) null).when(mockPubSub).unsubscribeFromTopic(anyString());
        Mockito.doReturn((Task<Void>) null).when(mockPubSub).subscribeToTopic(anyString());

        Provider gcmPubSubProvider = new Provider<FirebaseMessaging>() {

            @Override
            public FirebaseMessaging get(Object... in) {
                return mockPubSub;
            }


        };
        ;
        UnitTestUtils.setPrivateField(registrar, "firebaseMessagingProvider", gcmPubSubProvider);


        registrar.register(getContext(), callback);
        if (!latch.await(60, TimeUnit.SECONDS)) {
            try {
                writeLogcatLogs();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
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
        String jsonData = new FCMSharedPreferenceProvider().get(getContext()).getString(TEST_REGISTRAR_PREFERENCES_KEY, TAG);
        Assert.assertNotNull(jsonData);
        Assert.assertEquals(UnitTestUtils.getPrivateField(registrar, "deviceToken"), new JSONObject(jsonData).getString("deviceToken"));
        Assert.assertEquals(new JSONArray(Arrays.asList(CATEGORIES)).length(), new JSONObject(jsonData).getJSONArray("categories").length());
        Mockito.verify(mockPubSub, Mockito.times(1)).subscribeToTopic("test");
        Mockito.verify(mockPubSub, Mockito.times(1)).subscribeToTopic("anotherTest");
        Mockito.verify(mockPubSub, Mockito.times(1)).subscribeToTopic(TEST_SENDER_VARIANT);
    }

    @Test
    public void testUnregister() throws Exception {
        AeroGearFCMPushConfiguration config = new AeroGearFCMPushConfiguration()
                .setSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setCategories(CATEGORIES)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearFCMPushRegistrar registrar = (AeroGearFCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        StubHttpProvider provider = new StubHttpProvider();
        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);

        StubInstanceIDProvider firebaseInstanceIdProvider = new StubInstanceIDProvider();
        UnitTestUtils.setPrivateField(registrar, "firebaseInstanceIdProvider", firebaseInstanceIdProvider);

        final FirebaseMessaging mockPubSub = mock(FirebaseMessaging.class);
        Mockito.doReturn((Task<Void>) null).when(mockPubSub).unsubscribeFromTopic(anyString());
        Mockito.doReturn((Task<Void>) null).when(mockPubSub).subscribeToTopic(anyString());

        Provider gcmPubSubProvider = new Provider<FirebaseMessaging>() {

            @Override
            public FirebaseMessaging get(Object... in) {
                return mockPubSub;
            }


        };
        ;
        UnitTestUtils.setPrivateField(registrar, "firebaseMessagingProvider", gcmPubSubProvider);


        VoidCallback callback = new VoidCallback(latch);

        AeroGearFCMPushRegistrar spy = Mockito.spy(registrar);

        spy.register(getContext(), callback);
        latch.await(1, TimeUnit.SECONDS);

        latch = new CountDownLatch(1);
        callback = new VoidCallback(latch);
        spy.unregister(getContext(), callback);
        latch.await(1, TimeUnit.SECONDS);

        if (callback.exception != null) {
            Log.e(TAG, callback.exception.getMessage(), callback.exception);
            Assert.fail(callback.exception.getMessage());
        }

        Mockito.verify(firebaseInstanceIdProvider.mock).deleteInstanceId();
        Mockito.verify(provider.mock).delete(Mockito.matches("tempId"));
        Mockito.verify(mockPubSub, Mockito.times(1)).unsubscribeFromTopic("test");
        Mockito.verify(mockPubSub, Mockito.times(1)).unsubscribeFromTopic("anotherTest");
        Mockito.verify(mockPubSub, Mockito.times(1)).unsubscribeFromTopic(TEST_SENDER_VARIANT);
        Assert.assertNull(callback.exception);
        Assert.assertEquals("", UnitTestUtils.getPrivateField(registrar, "deviceToken"));
    }

    @Test
    public void testRegisterExceptionsAreCaught() throws Exception {
        AeroGearFCMPushConfiguration config = new AeroGearFCMPushConfiguration()
                .setSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearFCMPushRegistrar registrar = (AeroGearFCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        VoidCallback callback = new VoidCallback(latch);

        StubInstanceIDProvider firebaseInstanceIdProvider = new StubInstanceIDProvider();
        UnitTestUtils.setPrivateField(registrar, "firebaseInstanceIdProvider", firebaseInstanceIdProvider);

        registrar.register(getContext(), callback);
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

        AeroGearFCMPushRegistrar registrar = new AeroGearFCMPushRegistrar(config);
        CountDownLatch latch = new CountDownLatch(1);
        VoidCallback callback = new VoidCallback(latch);

        StubInstanceIDProvider firebaseInstanceIdProvider = new StubInstanceIDProvider();
        UnitTestUtils.setPrivateField(registrar, "firebaseInstanceIdProvider", firebaseInstanceIdProvider);

        registrar.unregister(getContext(), callback);
        latch.await(1, TimeUnit.SECONDS);
        Assert.assertNotNull(callback.exception);
        Assert.assertFalse(callback.exception instanceof IOException);
    }

    @Test
    public void testUnregisterTwice() throws Exception {

        AeroGearFCMPushConfiguration config = new AeroGearFCMPushConfiguration()
                .setSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearFCMPushRegistrar registrar = (AeroGearFCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        StubHttpProvider provider = new StubHttpProvider();

        final FirebaseMessaging mockPubSub = mock(FirebaseMessaging.class);
        Mockito.doReturn((Task<Void>) null).when(mockPubSub).unsubscribeFromTopic(anyString());
        Mockito.doReturn((Task<Void>) null).when(mockPubSub).subscribeToTopic(anyString());

        Provider gcmPubSubProvider = new Provider<FirebaseMessaging>() {

            @Override
            public FirebaseMessaging get(Object... in) {
                return mockPubSub;
            }


        };
        ;
        UnitTestUtils.setPrivateField(registrar, "firebaseMessagingProvider", gcmPubSubProvider);


        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);

        StubInstanceIDProvider firebaseInstanceIdProvider = new StubInstanceIDProvider();
        UnitTestUtils.setPrivateField(registrar, "firebaseInstanceIdProvider", firebaseInstanceIdProvider);

        VoidCallback callback = new VoidCallback(latch);

        AeroGearFCMPushRegistrar spy = Mockito.spy(registrar);

        spy.register(getContext(), callback);
        latch.await(1, TimeUnit.SECONDS);

        latch = new CountDownLatch(1);
        callback = new VoidCallback(latch);
        spy.unregister(getContext(), callback);
        latch.await(4, TimeUnit.SECONDS);

        Assert.assertNull(callback.exception);

        latch = new CountDownLatch(1);
        callback = new VoidCallback(latch);
        spy.unregister(getContext(), callback);
        latch.await(4, TimeUnit.SECONDS);

        Assert.assertNotNull(callback.exception);
        Assert.assertTrue(callback.exception instanceof IllegalStateException);

    }

    @Test
    public void testRegistrationTokensAreNotCached() throws Exception {

        AeroGearFCMPushConfiguration config = new AeroGearFCMPushConfiguration()
                .setSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearFCMPushRegistrar registrar = (AeroGearFCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);

        final FirebaseMessaging mockPubSub = mock(FirebaseMessaging.class);
        Mockito.doReturn((Task<Void>) null).when(mockPubSub).unsubscribeFromTopic(anyString());
        Mockito.doReturn((Task<Void>) null).when(mockPubSub).subscribeToTopic(anyString());

        Provider gcmPubSubProvider = new Provider<FirebaseMessaging>() {

            @Override
            public FirebaseMessaging get(Object... in) {
                return mockPubSub;
            }


        };

        UnitTestUtils.setPrivateField(registrar, "firebaseMessagingProvider", gcmPubSubProvider);

        StubHttpProvider provider = new StubHttpProvider();
        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);

        StubInstanceIDProvider firebaseInstanceIdProvider = new StubInstanceIDProvider();
        UnitTestUtils.setPrivateField(registrar, "firebaseInstanceIdProvider", firebaseInstanceIdProvider);

        UnitTestUtils.setPrivateField(registrar, "preferenceProvider", new Provider<SharedPreferences>() {

            @Override
            public SharedPreferences get(Object... in) {
                return new FCMSharedPreferenceProvider().get(getContext());
            }
        });

        VoidCallback callback = new VoidCallback(latch);

        registrar.register(getContext(), callback);
        latch.await(5, TimeUnit.SECONDS);
        Assert.assertNotNull(new FCMSharedPreferenceProvider().get(getContext()).getString("org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushRegistrar:" + TEST_SENDER_ID, null));

        latch = new CountDownLatch(1);
        callback = new VoidCallback(latch);
        registrar.unregister(getContext(), callback);
        latch.await(5, TimeUnit.SECONDS);
        Assert.assertNull(callback.exception);
        Assert.assertNull(new FCMSharedPreferenceProvider().get(getContext()).getString("org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushRegistrar:" + TEST_SENDER_ID, null));
        Mockito.verify(firebaseInstanceIdProvider.mock, Mockito.times(1)).deleteInstanceId();
    }

    @Test
    public void testAeroGearGCMPushConfigurationWithoutVariantID() throws Exception {

        try {

            AeroGearFCMPushConfiguration config = new AeroGearFCMPushConfiguration();
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

            AeroGearFCMPushConfiguration config = new AeroGearFCMPushConfiguration();
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

    private void writeLogcatLogs() throws IOException {
        Process process = Runtime.getRuntime().exec("logcat -d");
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        StringBuilder log = new StringBuilder();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            log.append(line);
        }
        Log.e(TAG, log.toString());
    }

    static class StubHttpProvider implements Provider<HttpProvider> {

        protected final HttpProvider mock = mock(HttpProvider.class);

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

    static class StubInstanceIDProvider implements Provider<FirebaseInstanceId> {

        private static final String TEMP_ID = "tempId";
        protected final FirebaseInstanceId mock = mock(FirebaseInstanceId.class);


        public StubInstanceIDProvider() {
            Task<InstanceIdResult> mockInstanceIdTask = mock(Task.class);
            when(mockInstanceIdTask.isComplete()).thenReturn(true);
            when(mockInstanceIdTask.isSuccessful()).thenReturn(true);
            when(mockInstanceIdTask.getResult()).thenReturn(new InstanceIdResult() {
                @NonNull
                @Override
                public String getId() {
                    return TEST_SENDER_ID;
                }

                @NonNull
                @Override
                public String getToken() {
                    return TEMP_ID;
                }
            });
            when(mockInstanceIdTask.addOnCompleteListener((OnCompleteListener<InstanceIdResult>) any())).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {

                    Task<InstanceIdResult> completedTask = mock(Task.class);
                    when(completedTask.isComplete()).thenReturn(true);
                    when(completedTask.isSuccessful()).thenReturn(true);
                    when(completedTask.getResult()).thenReturn(new InstanceIdResult() {
                        @NonNull
                        @Override
                        public String getId() {
                            return TEST_SENDER_ID;
                        }

                        @NonNull
                        @Override
                        public String getToken() {
                            return TEMP_ID;
                        }
                    });

                    OnCompleteListener<InstanceIdResult> listener = (OnCompleteListener<InstanceIdResult>) invocation.getArguments()[0];
                    listener.onComplete(completedTask);
                    return null;
                }
            });

            when(mock.getInstanceId()).thenReturn(mockInstanceIdTask);
        }

        @Override
        public FirebaseInstanceId get(Object... in) {
            return mock;
        }
    }

}

