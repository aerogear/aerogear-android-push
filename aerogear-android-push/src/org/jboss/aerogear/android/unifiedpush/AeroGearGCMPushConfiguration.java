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
package org.jboss.aerogear.android.unifiedpush;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Push Configuration which builds {@link AeroGearGCMPushRegistrar} instances.
 */
public class AeroGearGCMPushConfiguration extends PushConfiguration<AeroGearGCMPushConfiguration> {
    
    private static final long serialVersionUID = 1L;
    private String deviceToken = "";
    private String variantID;
    private String secret;
    private String deviceType = "ANDROID";
    private String operatingSystem = "android";
    private String osVersion = android.os.Build.VERSION.RELEASE;
    private String alias;
    private List<String> categories = new ArrayList<String>();
    private URI pushServerURI;
    private Set<String> senderIds = new HashSet<String>();

    
    
    /**
     * The device token Identifies the device within its Push Network. It is the
     * value = GoogleCloudMessaging.getInstance(context).register(SENDER_ID);
     * 
     * @return the current device token
     * 
     */
    public String getDeviceToken() {
        return deviceToken;
    }

    /**
     * The device token Identifies the device within its Push Network. It is the
     * value = GoogleCloudMessaging.getInstance(context).register(SENDER_ID);
     * 
     * @param deviceToken the new device token
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushConfiguration setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        return this;
    }

    /**
     * ID of the Variant from the AeroGear UnifiedPush Server.
     * 
     * @return the current variant id
     */
    public String getVariantID() {
        return variantID;
    }

    /**
     * ID of the Variant from the AeroGear UnifiedPush Server.
     * 
     * @param variantID the new variantID
     * @return the current configuration
     */
    public AeroGearGCMPushConfiguration setVariantID(String variantID) {
        this.variantID = variantID;
        return this;
    }

    /**
     * Secret of the Variant from the AeroGear UnifiedPush Server.     
     * 
     * @return the current Secret
     * 
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Secret of the Variant from the AeroGear UnifiedPush Server.
     * 
     * @param secret the new secret
     * @return the current configuration
     */
    public AeroGearGCMPushConfiguration setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * Device type determines which cloud messaging system will be used by the
     * AeroGear Unified Push Server
     *
     * Defaults to ANDROID
     * 
     * @return the device type
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Device type determines which cloud messaging system will be used by the
     * AeroGear Unified Push Server.
     *
     * Defaults to ANDROID
     * 
     * @param deviceType a new device type
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushConfiguration setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    /**
     * The name of the operating system. Defaults to Android
     * 
     * @return the operating system
     */
    public String getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * The name of the operating system. Defaults to Android
     * 
     * @param operatingSystem the new operating system
     * @return the current configuration
     */
    public AeroGearGCMPushConfiguration setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
        return this;
    }

    /**
     * The version of the operating system running.
     *
     * Defaults to the value provided by android.os.Build.VERSION.RELEASE
     * 
     * @return the current OSversion
     * 
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * The version of the operating system running.
     *
     * Defaults to the value provided by android.os.Build.VERSION.RELEASE
     * 
     * @param osVersion the new osVersion
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushConfiguration setOsVersion(String osVersion) {
        this.osVersion = osVersion;
        return this;
    }

    /**
     * The Alias is an identifier of the user of the system.
     *
     * Examples are an email address or a username
     *
     * @return alias
     * 
     */
    public String getAlias() {
        return alias;
    }

    /**
     * The Alias is an identifier of the user of the system.
     *
     * Examples are an email address or a username
     *
     * @param alias the new alias
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushConfiguration setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * The categories specifies a channel which may be used to send messages
     * 
     * @return the current categories
     * 
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * The categories specifies a channel which may be used to send messages
     * 
     * @param categories the new categories
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushConfiguration setCategories(List<String> categories) {
        this.categories = new ArrayList<String>(categories);
        return this;
    }

    /**
     * The categories specifies a channel which may be used to send messages
     * 
     * @param categories the new categories
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushConfiguration setCategories(String... categories) {
        this.categories = Arrays.asList(categories);
        return this;
    }
    
    /**
     * The categories specifies a channel which may be used to send messages
     * 
     * @param category a new category to be added to the current list.
     * @return the current configuration
     * 
     */
    public AeroGearGCMPushConfiguration addCategory(String category) {
        categories.add(category);
        return this;
    }

    /**
     * RegistryURL is the URL of the 3rd party application server
     *
     * @return the current pushServerURI
     */
    public URI getPushServerURI() {
        return pushServerURI;
    }

    /**
     * RegistryURL is the URL of the 3rd party application server
     * 
     * @param pushServerURI a new URI
     * @return the current configuration
     *
     */
    public AeroGearGCMPushConfiguration setPushServerURI(URI pushServerURI) {
        this.pushServerURI = pushServerURI;
        return this;
    }

    /**
     * SenderIds is a collection of all GCM sender Id elements registered for 
     * this application.
     * 
     * @return a copy of the current set of senderIds.
     * 
     */ 
    public Set<String> getSenderIds() {
        return new HashSet<String>(senderIds);
    }
    
    /**
     * SenderIds is a collection of all GCM sender Id elements registered for 
     * this application.
     * 
     * @param senderIds the new sender Ids to set.
     * @return the current configuration.
     */
    public AeroGearGCMPushConfiguration setSenderIds(String... senderIds) {
        Set<String> newSenderIds = new HashSet<String>(senderIds.length);
        Collections.addAll(newSenderIds, senderIds);
        this.senderIds = newSenderIds;
        return this;
    }
    
    /**
     * SenderIds is a collection of all GCM sender Id elements registered for 
     * this application.
     * 
     * @param senderId a new sender Id to add to the current set of senderIds.
     * @return the current configuration.
     */
    public AeroGearGCMPushConfiguration addSenderId(String senderId) {
        this.senderIds.add(senderId);
        return this;
    }

    
    /**
     * 
     * Protected builder method.
     * 
     * @return A configured AeroGearGCMPushRegistrar
     * 
     * @throws IllegalStateException if pushServerURI is null or if senderIds is null or empty.
     */
    @Override
    protected final AeroGearGCMPushRegistrar buildRegistrar() {
        
        if (senderIds == null || senderIds.isEmpty()) {
            throw new IllegalStateException("SenderIds can't be null or empty");
        }
        
        if (pushServerURI == null) {
            throw new IllegalStateException("PushServerURI can't be null");
        }
        
        return new AeroGearGCMPushRegistrar(this);
    }
    
}
