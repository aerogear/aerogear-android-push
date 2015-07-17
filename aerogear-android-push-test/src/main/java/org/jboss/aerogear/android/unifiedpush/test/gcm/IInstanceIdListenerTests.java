package org.jboss.aerogear.android.unifiedpush.test.gcm;

import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;
import com.google.android.gms.iid.InstanceID;
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
import org.jboss.aerogear.android.unifiedpush.test.util.UnitTestUtils;
import org.jboss.aerogear.android.unifiedpush.test.util.VoidCallback;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
public class IInstanceIdListenerTests extends PatchedActivityInstrumentationTestCase {

    private static final String TEST_SENDER_ID = "272275396485";
    private static final String TEST_REGISTRAR_PREFERENCES_KEY = "org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushRegistrar:272275396485";
    private static final String TEST_SENDER_PASSWORD = "Password";
    private static final String TEST_SENDER_VARIANT = "Variant";
    
    public IInstanceIdListenerTests() {
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

        registrar.register(super.getActivity(), callback);
        if (!latch.await(30, TimeUnit.SECONDS)) {
            Assert.fail("Latch wasn't called");
        }
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
        
        UnitTestUtils.setPrivateField(service, "instanceIdProvider", new Provider<InstanceID>() {

            @Override
            public InstanceID get(Object... in) {
               return InstanceID.getInstance(getActivity());
            }
        });
        
        
        
        service.onTokenRefresh();
        
        Mockito.verify(httpProvider.get()).post(Matchers.anyString());
        
    }
    
}
