/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.unifiedpush.test;

import android.support.test.runner.AndroidJUnit4;

import org.jboss.aerogear.android.core.ConfigurationProvider;
import org.jboss.aerogear.android.unifiedpush.PushConfiguration;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.RegistrarManager;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushConfiguration;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushRegistrar;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class RegistrationsTest {

    private static final String PUSH = "push";

    @Test
    public void testDefaultConfig() throws URISyntaxException {

        PushConfiguration config = RegistrarManager
                .config(PUSH, AeroGearFCMPushConfiguration.class)
                .setPushServerURI(new URI("http://testreg.com"))
                .setSenderId("TestID")
                .setVariantID("VariantID")
                .setSecret("secret");

        config.asRegistrar();
        PushRegistrar registrar = RegistrarManager.getRegistrar(PUSH);
        Assert.assertNotNull(registrar);
        Assert.assertTrue(registrar instanceof AeroGearFCMPushRegistrar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsOnUnsupportedType() {
        PushConfiguration config = RegistrarManager.config(PUSH, BrokenConfig.class);
    }

    @Test
    public void testCustomFactoryType() {

        RegistrarManager.registerConfigurationProvider(StubConfig.class, new ConfigurationProvider<StubConfig>() {

            @Override
            public StubConfig newConfiguration() {
                return new StubConfig();
            }
        });

        StubConfig config = RegistrarManager.config(PUSH, StubConfig.class);
        config.asRegistrar();
        Assert.assertNotNull(RegistrarManager.getRegistrar(PUSH));
        Assert.assertFalse(RegistrarManager.getRegistrar(PUSH) instanceof AeroGearFCMPushRegistrar);

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
