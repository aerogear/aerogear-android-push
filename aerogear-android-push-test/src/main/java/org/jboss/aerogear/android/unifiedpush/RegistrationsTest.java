/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.android.unifiedpush;

import java.net.URI;
import java.net.URISyntaxException;
import org.jboss.aerogear.android.ConfigurationProvider;
import org.jboss.aerogear.android.impl.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.unifiedpush.test.MainActivity;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushConfiguration;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushRegistrar;
import org.jboss.aerogear.android.unifiedpush.PushConfiguration;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.RegistrarManager;
import static org.mockito.Mockito.mock;

public class RegistrationsTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    public RegistrationsTest() {
        super(MainActivity.class);
    }

    private static final String PUSH = "push";

    public void testDefaultConfig() throws URISyntaxException {

        PushConfiguration config = RegistrarManager
                .config(PUSH, AeroGearGCMPushConfiguration.class)
                .setSenderIds("TestID")
                .setPushServerURI(new URI("http://testreg.com"));
        config.asRegistrar();
        PushRegistrar registrar = RegistrarManager.getRegistrar(PUSH);
        assertNotNull(registrar);
        assertTrue(registrar instanceof AeroGearGCMPushRegistrar);
    }

    public void testFailsOnUnsupportedType() {
        try {
            PushConfiguration config = RegistrarManager.config(PUSH, BrokenConfig.class);
            fail(); // expect IllegalArgumentException
        } catch (IllegalArgumentException ignore) {
            return;//pass()
        }
    }

    public void testCustomFactoryType() {

        RegistrarManager.registerConfigurationProvider(StubConfig.class, new ConfigurationProvider<StubConfig>() {

            @Override
            public StubConfig newConfiguration() {
                return new StubConfig();
            }
        });

        StubConfig config = RegistrarManager.config(PUSH, StubConfig.class);
        config.asRegistrar();
        assertNotNull(RegistrarManager.getRegistrar(PUSH));
        assertFalse(RegistrarManager.getRegistrar(PUSH) instanceof AeroGearGCMPushRegistrar);

    }

    private static final class BrokenConfig extends PushConfiguration<BrokenConfig> {

        @Override
        protected PushRegistrar buildRegistrar() {
            return mock(PushRegistrar.class);
        }

    }
    
    private static final class StubConfig extends PushConfiguration<StubConfig> {

        @Override
        protected PushRegistrar buildRegistrar() {
            return mock(PushRegistrar.class);
        }

    }

}
