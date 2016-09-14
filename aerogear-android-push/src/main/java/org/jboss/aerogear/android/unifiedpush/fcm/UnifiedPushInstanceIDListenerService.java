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
package org.jboss.aerogear.android.unifiedpush.fcm;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import com.google.gson.JsonObject;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.pipe.http.HttpProvider;
import org.jboss.aerogear.android.pipe.http.HttpRestProvider;
import static org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushRegistrar.REGISTRAR_PREFERENCE_PATTERN;

/**
 * This is an Android Service which listens for InstanceID messages from
 * Google's FCM services.  These messages arrive periodically from Google's 
 * systems to alert the application it needs to refresh its registration tokens.
 *
 * See
 * https://developers.google.com/instance-id/guides/android-implementation#refresh_tokens
 * for official docs
 *
 */
public class UnifiedPushInstanceIDListenerService extends FirebaseInstanceIdService {

    private final static String BASIC_HEADER = "Authorization";
    private final static String AUTHORIZATION_METHOD = "Basic";

    private static final String TAG = UnifiedPushInstanceIDListenerService.class.getSimpleName();
    private static final Integer TIMEOUT = 30000;// 30 seconds

    private final Provider<SharedPreferences> sharedPreferencesProvider = new FCMSharedPreferenceProvider();

    private final Provider<FirebaseInstanceId> instanceIdProvider = new Provider<FirebaseInstanceId>() {

        @Override
        public FirebaseInstanceId get(Object... context) {
            return FirebaseInstanceId.getInstance();
        }
    };

    private final Provider<HttpProvider> httpProviderProvider = new Provider<HttpProvider>() {

        @Override
        public HttpProvider get(Object... in) {
            return new HttpRestProvider((URL) in[0], (Integer) in[1]);
        }
    };

    @Override
    /**
     * This method is called when the Google Services have instructed us to
     * refresh out token states.
     */
    public void onTokenRefresh() {

        SharedPreferences sharedPreferences = sharedPreferencesProvider.get(this);

        Map<String, ?> preferences = sharedPreferences.getAll();

        for (Map.Entry<String, ?> preference : preferences.entrySet()) {
            if (preference.getKey().matches(REGISTRAR_PREFERENCE_PATTERN)) {

                FirebaseInstanceId instanceID = instanceIdProvider.get(this);
                try {
                    String token = instanceID.getToken();
                    JsonObject oldPostData = new JsonParser().parse(preference.getValue().toString()).getAsJsonObject();
                    String oldToken = "";
                    try {
                        oldToken = oldPostData.get("deviceToken").getAsString();
                    } catch (Exception exception) {
                        //We don't care if the old device token isn't set right.
                        Log.w(TAG, exception.getMessage(), exception);
                    }
                    URL deviceRegistryURL = new URL(oldPostData.get("deviceRegistryURL").getAsString());
                    String variantId = oldPostData.get("variantId").getAsString();
                    String secret = oldPostData.get("secret").getAsString();

                    HttpProvider httpProvider = httpProviderProvider.get(deviceRegistryURL, TIMEOUT);

                    httpProvider.setDefaultHeader("x-ag-old-token", oldToken);

                    setPasswordAuthentication(variantId, secret, httpProvider);

                    JsonObject postData = new JsonObject();
                    if (oldPostData.has("deviceType")&& !oldPostData.get("deviceType").isJsonNull()) {
                        postData.addProperty("deviceType", oldPostData.get("deviceType").getAsString());
                    }
                    postData.addProperty("deviceToken", token);
                    
                    if (oldPostData.has("alias") && !oldPostData.get("alias").isJsonNull()) {
                        postData.addProperty("alias", oldPostData.get("alias").getAsString());
                    }
                    if (oldPostData.has("operatingSystem")&& !oldPostData.get("operatingSystem").isJsonNull()) {
                        postData.addProperty("operatingSystem", oldPostData.get("operatingSystem").getAsString());
                    }
                    if (oldPostData.has("osVersion")&& !oldPostData.get("osVersion").isJsonNull()) {
                        postData.addProperty("osVersion", oldPostData.get("osVersion").getAsString());
                    }
                    if (oldPostData.has("categories")&& !oldPostData.get("categories").isJsonNull()) {
                        postData.add("categories", oldPostData.get("categories").getAsJsonArray());
                    }

                    httpProvider.post(postData.toString());

                } catch (IOException ex) {
                    Log.e(TAG, ex.getMessage(), ex);
                }

            }
        }

    }

    private void setPasswordAuthentication(final String username, final String password, final HttpProvider provider) {
        provider.setDefaultHeader(BASIC_HEADER, getHashedAuth(username, password.toCharArray()));
    }

    private String getHashedAuth(String username, char[] password) {
        StringBuilder headerValueBuilder = new StringBuilder(AUTHORIZATION_METHOD).append(" ");
        String unhashedCredentials = new StringBuilder(username).append(":").append(password).toString();
        String hashedCrentials = Base64.encodeToString(unhashedCredentials.getBytes(), Base64.DEFAULT | Base64.NO_WRAP);
        return headerValueBuilder.append(hashedCrentials).toString();
    }

}
