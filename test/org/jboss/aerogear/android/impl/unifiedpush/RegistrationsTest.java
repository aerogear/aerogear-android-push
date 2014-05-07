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

import java.net.URI;
import java.net.URISyntaxException;
import org.jboss.aerogear.android.unifiedpush.PushConfig;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.PushRegistrarFactory;
import org.jboss.aerogear.android.unifiedpush.PushType;
import org.jboss.aerogear.android.unifiedpush.Registrations;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

public class RegistrationsTest {

    private static final String PUSH = "push";

    @Test
    public void testDefaultConfig() throws URISyntaxException {
        Registrations reg = new Registrations();
        PushConfig config = new PushConfig(new URI("http://testreg.com"), "TestID");
        reg.push(PUSH, config);
        PushRegistrar registrar = reg.get(PUSH);
        assertNotNull(registrar);
        assertTrue(registrar instanceof AeroGearGCMPushRegistrar);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsOnUnsupportedType() throws URISyntaxException {
        Registrations reg = new Registrations();
        PushConfig config = new PushConfig(new URI("http://testreg.com"), "TestID");
        config.setType(new PushType() {

            @Override
            public String getName() {
                return "FAIL_TYPE";
            }
        });
        reg.push(PUSH, config);

    }

    @Test
    public void testCustomFactoryType() throws URISyntaxException {
        Registrations reg = new Registrations(new PushRegistrarFactory() {

            @Override
            public PushRegistrar createPushRegistrar(PushConfig config) {
                if (config.getType().getName().equals("CUSTOM_TYPE")) {
                    return Mockito.mock(PushRegistrar.class);
                }
                else {
                    return null;
                }
            }
        });
        PushConfig config = new PushConfig(new URI("http://testreg.com"), "TestID");
        config.setType(new PushType() {

            @Override
            public String getName() {
                return "CUSTOM_TYPE";
            }
        });
        assertNotNull(reg.push(PUSH, config));

    }

}
