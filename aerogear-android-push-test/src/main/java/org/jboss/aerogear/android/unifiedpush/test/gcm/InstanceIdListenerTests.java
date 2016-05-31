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

import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushConfiguration;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushRegistrar;
import org.jboss.aerogear.android.unifiedpush.gcm.GCMSharedPreferenceProvider;
import org.jboss.aerogear.android.unifiedpush.gcm.UnifiedPushInstanceIDListenerService;
import org.jboss.aerogear.android.unifiedpush.test.MainActivity;
import org.jboss.aerogear.android.unifiedpush.test.util.PatchedActivityInstrumentationTestCase;
import com.google.firebase.messaging.FirebaseMessaging;
import org.jboss.aerogear.android.unifiedpush.test.util.UnitTestUtils;
import org.jboss.aerogear.android.unifiedpush.test.util.VoidCallback;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import static org.mockito.Matchers.anyString;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
public class InstanceIdListenerTests extends PatchedActivityInstrumentationTestCase {

    private static final String TEST_SENDER_ID = "272275396485";
    private static final String TEST_REGISTRAR_PREFERENCES_KEY = "org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushRegistrar:272275396485";
    private static final String TEST_SENDER_PASSWORD = "Password";
    private static final String TEST_SENDER_VARIANT = "Variant";

    public InstanceIdListenerTests() {
        super(MainActivity.class);
    }

    @Before
    public void fakeRegister() throws Exception {
        AeroGearGCMPushConfiguration config = new AeroGearGCMPushConfiguration()
                .setSenderId(TEST_SENDER_ID)
                .setVariantID(TEST_SENDER_VARIANT)
                .setSecret(TEST_SENDER_PASSWORD)
                .setPushServerURI(new URI("https://testuri"));

        AeroGearGCMPushRegistrar registrar = (AeroGearGCMPushRegistrar) config.asRegistrar();
        CountDownLatch latch = new CountDownLatch(1);
        AeroGearGCMPushRegistrarTest.StubHttpProvider provider = new AeroGearGCMPushRegistrarTest.StubHttpProvider();
        UnitTestUtils.setPrivateField(registrar, "httpProviderProvider", provider);
        VoidCallback callback = new VoidCallback(latch);
        UnitTestUtils.setPrivateField(registrar, "firebaseInstanceIdProvider", new AeroGearGCMPushRegistrarTest.StubInstanceIDProvider());

        final FirebaseMessaging mockPubSub = Mockito.mock(FirebaseMessaging.class);
        Mockito.doNothing().when(mockPubSub).unsubscribeFromTopic(anyString());
        Mockito.doNothing().when(mockPubSub).subscribeToTopic(anyString());

        Provider gcmPubSubProvider = new Provider<FirebaseMessaging>() {

            @Override
            public FirebaseMessaging get(Object... in) {
                return mockPubSub;
            }

        };;

        UnitTestUtils.setPrivateField(registrar, "firebaseMessagingProvider", gcmPubSubProvider);
        registrar.register(super.getActivity(), callback);

        if (!latch.await(30, TimeUnit.SECONDS)) {
            Assert.fail("Latch wasn't called");
        }

        if (callback.exception != null) {
            throw callback.exception;
        }

        Assert.assertNotNull(new GCMSharedPreferenceProvider().get(getActivity()).getString(TEST_REGISTRAR_PREFERENCES_KEY, null));
    }

    @Test
    public void refreshIntentSendsCallsRefresh() throws Exception {
        AeroGearGCMPushRegistrarTest.StubHttpProvider httpProvider = new AeroGearGCMPushRegistrarTest.StubHttpProvider();

        UnifiedPushInstanceIDListenerService service = new UnifiedPushInstanceIDListenerService();
        UnitTestUtils.setPrivateField(service, "httpProviderProvider", httpProvider);

        UnitTestUtils.setPrivateField(service, "sharedPreferencesProvider", new Provider<SharedPreferences>() {

            @Override
            public SharedPreferences get(Object... in) {
                return new GCMSharedPreferenceProvider().get(getActivity());
            }
        });

        UnitTestUtils.setPrivateField(service, "instanceIdProvider", new AeroGearGCMPushRegistrarTest.StubInstanceIDProvider());

        service.onTokenRefresh();

        Mockito.verify(httpProvider.get()).post(Matchers.anyString());

    }

}
