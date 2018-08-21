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
package org.jboss.aerogear.android.unifiedpush.fcm;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

import org.jboss.aerogear.android.core.Provider;
import org.jboss.aerogear.android.pipe.http.HttpProvider;
import org.jboss.aerogear.android.pipe.http.HttpRestProvider;
import org.jboss.aerogear.android.unifiedpush.MessageHandler;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.aerogear.android.unifiedpush.RegistrarManager;

import static org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushRegistrar.REGISTRAR_PREFERENCE_PATTERN;

/**
 * <p>
 * AeroGear specific <code>BroadcastReceiver</code> implementation for Firebase
 * Cloud Messaging.
 *
 * <p>
 * Internally received messages are delivered to attached implementations of our
 * <code>MessageHandler</code> interface.
 */
public class AeroGearUPSMessageService extends FirebaseMessagingService {

    public static final int NOTIFICATION_ID = 1;

    private static MessageHandler defaultHandler;
    private static boolean checkDefaultHandler = true;
    private static final String TAG = AeroGearUPSMessageService.class.getSimpleName();
    public static final String DEFAULT_MESSAGE_HANDLER_KEY = "DEFAULT_MESSAGE_HANDLER_KEY";


    private final static String BASIC_HEADER = "Authorization";
    private final static String AUTHORIZATION_METHOD = "Basic";

    private static final Integer TIMEOUT = 30000;// 30 seconds

    private final Provider<SharedPreferences> sharedPreferencesProvider = new FCMSharedPreferenceProvider();

    private final Provider<HttpProvider> httpProviderProvider = new Provider<HttpProvider>() {

        @Override
        public HttpProvider get(Object... in) {
            return new HttpRestProvider((URL) in[0], (Integer) in[1]);
        }
    };

    @Override
    /**
     * When a FCM message is received, the attached implementations of our
     * <code>MessageHandler</code> interface are being notified.
     */

    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> messageMap = remoteMessage.getData();

        Bundle message = new Bundle();

        for (Map.Entry<String, String> messageMapEntry : messageMap.entrySet()) {
            message.putString(messageMapEntry.getKey(), messageMapEntry.getValue());
        }

        if (checkDefaultHandler) {
            checkDefaultHandler = false;
            Bundle metaData = getMetadata(getApplicationContext());
            if (metaData != null) {

                String defaultHandlerClassName = metaData.getString(DEFAULT_MESSAGE_HANDLER_KEY);
                if (defaultHandlerClassName != null) {
                    try {
                        Class<? extends MessageHandler> defaultHandlerClass = (Class<? extends MessageHandler>) Class.forName(defaultHandlerClassName);
                        defaultHandler = defaultHandlerClass.newInstance();
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage(), ex);
                    }

                }
            }
        }

        // notity all attached MessageHandler implementations:
        RegistrarManager.notifyHandlers(getApplicationContext(), message, defaultHandler);
    }

    private Bundle getMetadata(Context context) {
        try {
            final ComponentName componentName = new ComponentName(context, AeroGearUPSMessageService.class);
            ApplicationInfo si = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = si.metaData;
            if (metaData == null) {
                Log.d(TAG, "metaData is null. Unable to get meta data for " + componentName);
            } else {
                return metaData;
            }

        } catch (PackageManager.NameNotFoundException ex) {
            Logger.getLogger(AeroGearUPSMessageService.class.getName()).log(Level.SEVERE, null, ex);

        }

        return null;
    }

    @Override
    /**
     * This method is called when the Google Services have instructed us to
     * refresh out token states.
     */
    public void onNewToken(String token) {

        SharedPreferences sharedPreferences = sharedPreferencesProvider.get(this);

        Map<String, ?> preferences = sharedPreferences.getAll();

        for (Map.Entry<String, ?> preference : preferences.entrySet()) {
            if (preference.getKey().matches(REGISTRAR_PREFERENCE_PATTERN)) {

                try {

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
                    if (oldPostData.has("deviceType") && !oldPostData.get("deviceType").isJsonNull()) {
                        postData.addProperty("deviceType", oldPostData.get("deviceType").getAsString());
                    }
                    postData.addProperty("deviceToken", token);

                    if (oldPostData.has("alias") && !oldPostData.get("alias").isJsonNull()) {
                        postData.addProperty("alias", oldPostData.get("alias").getAsString());
                    }
                    if (oldPostData.has("operatingSystem") && !oldPostData.get("operatingSystem").isJsonNull()) {
                        postData.addProperty("operatingSystem", oldPostData.get("operatingSystem").getAsString());
                    }
                    if (oldPostData.has("osVersion") && !oldPostData.get("osVersion").isJsonNull()) {
                        postData.addProperty("osVersion", oldPostData.get("osVersion").getAsString());
                    }
                    if (oldPostData.has("categories") && !oldPostData.get("categories").isJsonNull()) {
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
