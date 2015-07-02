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
package org.jboss.aerogear.android.unifiedpush.test.util;


import android.app.Activity;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import org.junit.Before;

/**
 * All tests which use Mockito should extend this class.
 * 
 * In Android 4.3 the dexcache is not set as it was previously and Mockito will
 * throw an Exception. This class sets the dexcache directory correctly during
 * test setup.
 * 
 */
public abstract class PatchedActivityInstrumentationTestCase {

    
    private final ActivityTestRule rule;

    public PatchedActivityInstrumentationTestCase(Class<? extends Activity> klass) {
        rule = new ActivityTestRule(klass, false, false);
    }
        
    @Before
    /**
     * Sets the dexcache property before test execution.
     */
    public void setDexcacheDir() throws Exception {
        rule.launchActivity(new Intent(Intent.ACTION_MAIN));
        System.setProperty("dexmaker.dexcache", rule.getActivity().getCacheDir().getPath());
    }

    public Activity getActivity() {
        return rule.getActivity();
    }
}