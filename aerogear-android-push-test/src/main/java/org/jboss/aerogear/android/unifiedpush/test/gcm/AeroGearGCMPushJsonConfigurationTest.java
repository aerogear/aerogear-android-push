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

import junit.framework.Assert;
import org.jboss.aerogear.android.unifiedpush.gcm.AeroGearGCMPushJsonConfiguration;
import org.jboss.aerogear.android.unifiedpush.test.MainActivity;
import org.jboss.aerogear.android.unifiedpush.test.util.PatchedActivityInstrumentationTestCase;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AeroGearGCMPushJsonConfigurationTest
        extends PatchedActivityInstrumentationTestCase<MainActivity> {

    private static final URI pushServerURL = URI.create("https://localhost:8080/ag-push");
    private static final Set<String> senderIds = new HashSet<String>(Arrays.asList("123456"));
    private static final String variantID = "8abfae4eb02a6140c0a20798433180a063fd7006";
    private static final String secret = "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8";

    public AeroGearGCMPushJsonConfigurationTest() {
        super(MainActivity.class);
    }

    public void testJsonConfigUsingDefaultFileName() {
        AeroGearGCMPushJsonConfiguration config = new AeroGearGCMPushJsonConfiguration();
        config.loadConfigJson(getActivity());

        Assert.assertEquals(pushServerURL, config.getPushServerURI());
        Assert.assertEquals(senderIds.iterator().next(), config.getSenderIds().iterator().next());
        Assert.assertEquals(variantID, config.getVariantID());
        Assert.assertEquals(secret, config.getSecret());
    }

    public void testJsonConfigUsingDifferentFileName() {
        AeroGearGCMPushJsonConfiguration config = new AeroGearGCMPushJsonConfiguration();
        config.setFileName("correct-config.json");
        config.loadConfigJson(getActivity());

        Assert.assertEquals(pushServerURL, config.getPushServerURI());
        Assert.assertEquals(senderIds.iterator().next(), config.getSenderIds().iterator().next());
        Assert.assertEquals(variantID, config.getVariantID());
        Assert.assertEquals(secret, config.getSecret());
    }

    public void testJsonConfigUsingIncorrectFileFormat() {
        AeroGearGCMPushJsonConfiguration config = new AeroGearGCMPushJsonConfiguration();
        config.setFileName("wrong-format.json");

        try {
            config.loadConfigJson(getActivity());
            fail("Somethings is wrong. File with incorrect format should throw an exception");
        } catch (RuntimeException e) {
            String errorMessage = "An error occurred while parsing the wrong-format.json. Please check the file format";
            Assert.assertEquals(errorMessage, e.getMessage());
        }

        Assert.assertNull(config.getPushServerURI());
        Assert.assertEquals(0, config.getSenderIds().size());
        Assert.assertNull(config.getVariantID());
        Assert.assertNull(config.getSecret());
    }

    public void testJsonConfigUsingEmptyFile() {
        AeroGearGCMPushJsonConfiguration config = new AeroGearGCMPushJsonConfiguration();
        config.setFileName("empty-file.json");

        try {
            config.loadConfigJson(getActivity());
            fail("Somethings is wrong. Empty file should throw an exception");
        } catch (RuntimeException e) {
            String errorMessage = "An error occurred while parsing the empty-file.json. Please check the file format";
            Assert.assertEquals(errorMessage, e.getMessage());
        }
    }

    public void testJsonConfigUsingAbsentFile() {
        AeroGearGCMPushJsonConfiguration config = new AeroGearGCMPushJsonConfiguration();
        config.setFileName("blablabla.json");
        try {
            config.loadConfigJson(getActivity());
            fail("Somethings is wrong. This file not exists and load should throw an exception");
        } catch (RuntimeException e) {
            String errorMessage = "An error occurred while parsing the blablabla.json. Please check if the file exists";
            Assert.assertEquals(errorMessage, e.getMessage());
        }
    }

}
