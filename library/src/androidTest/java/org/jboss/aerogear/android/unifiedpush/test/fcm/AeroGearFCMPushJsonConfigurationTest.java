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
package org.jboss.aerogear.android.unifiedpush.test.fcm;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushJsonConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static androidx.test.platform.app.InstrumentationRegistry.getContext;

@RunWith(AndroidJUnit4.class)
public class AeroGearFCMPushJsonConfigurationTest {

    private static final URI pushServerURL = URI.create("https://localhost:8080/ag-push");
    private static final String senderId = "123456";
    private static final String variantID = "8abfae4eb02a6140c0a20798433180a063fd7006";
    private static final String secret = "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8";


    @Test
    public void testJsonConfigUsingDefaultFileName() {
        AeroGearFCMPushJsonConfiguration config = new AeroGearFCMPushJsonConfiguration();
        config.loadConfigJson(getContext());

        Assert.assertEquals(pushServerURL, config.getPushServerURI());
        Assert.assertEquals(senderId, config.getSenderId());
        Assert.assertEquals(variantID, config.getVariantID());
        Assert.assertEquals(secret, config.getSecret());
    }

    @Test
    public void testJsonConfigUsingDifferentFileName() {
        AeroGearFCMPushJsonConfiguration config = new AeroGearFCMPushJsonConfiguration();
        config.setFileName("correct-config.json");
        config.loadConfigJson(getContext());

        Assert.assertEquals(pushServerURL, config.getPushServerURI());
        Assert.assertEquals(senderId, config.getSenderId());
        Assert.assertEquals(variantID, config.getVariantID());
        Assert.assertEquals(secret, config.getSecret());
    }

    @Test
    public void testJsonConfigUsingIncorrectFileFormat() {
        AeroGearFCMPushJsonConfiguration config = new AeroGearFCMPushJsonConfiguration();
        config.setFileName("wrong-format.json");

        try {
            config.loadConfigJson(getContext());
            Assert.fail("Somethings is wrong. File with incorrect format should throw an exception");
        } catch (RuntimeException e) {
            String errorMessage = "An error occurred while parsing the wrong-format.json. Please check the file format";
            Assert.assertEquals(errorMessage, e.getMessage());
        }

        Assert.assertNull(config.getPushServerURI());
        Assert.assertNull(config.getSenderId());
        Assert.assertNull(config.getVariantID());
        Assert.assertNull(config.getSecret());
    }

    @Test
    public void testJsonConfigUsingEmptyFile() {
        AeroGearFCMPushJsonConfiguration config = new AeroGearFCMPushJsonConfiguration();
        config.setFileName("empty-file.json");

        try {
            config.loadConfigJson(getContext());
            Assert.fail("Somethings is wrong. Empty file should throw an exception");
        } catch (RuntimeException e) {
            String errorMessage = "An error occurred while parsing the empty-file.json. Please check the file format";
            Assert.assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    public void testJsonConfigUsingAbsentFile() {
        AeroGearFCMPushJsonConfiguration config = new AeroGearFCMPushJsonConfiguration();
        config.setFileName("blablabla.json");
        try {
            config.loadConfigJson(getContext());
            Assert.fail("Somethings is wrong. This file not exists and load should throw an exception");
        } catch (RuntimeException e) {
            String errorMessage = "An error occurred while parsing the blablabla.json. Please check if the file exists";
            Assert.assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    public void testSetCategoriesUsingStringArray() {
        AeroGearFCMPushJsonConfiguration config = new AeroGearFCMPushJsonConfiguration();
        config.setCategories("A", "B");
        Assert.assertEquals(2, config.getCategories().size());
    }

    @Test
    public void testSetCategoriesUsingList() {
        List<String> myCategories = new ArrayList<String>();
        myCategories.add("A");
        myCategories.add("B");

        AeroGearFCMPushJsonConfiguration config = new AeroGearFCMPushJsonConfiguration();
        config.setCategories(myCategories);
        Assert.assertEquals(2, config.getCategories().size());
    }

}
