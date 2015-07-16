package org.jboss.aerogear.android.unifiedpush.test.gcm;

import android.support.test.runner.AndroidJUnit4;
import org.jboss.aerogear.android.unifiedpush.test.MainActivity;
import org.jboss.aerogear.android.unifiedpush.test.util.PatchedActivityInstrumentationTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class IInstanceIdListenerTests extends PatchedActivityInstrumentationTestCase {

    public IInstanceIdListenerTests() {
        super(MainActivity.class);
    }
    
    @Before
    public void fakeRegister() {
    }
    
    @Test
    public void refreshIntentSendsCallsRefresh() {
        Assert.fail();
    }
    
}
