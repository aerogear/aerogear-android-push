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

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import org.jboss.aerogear.android.unifiedpush.MessageHandler;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.aerogear.android.unifiedpush.RegistrarManager;

/**
 * <p>
 * AeroGear specific <code>BroadcastReceiver</code> implementation for Firebase
 * Cloud Messaging.
 *
 * <p>
 * Internally received messages are delivered to attached implementations of our
 * <code>MessageHandler</code> interface.
 */
public class AeroGearFCMMessageReceiver extends FirebaseMessagingService {

    public static final int NOTIFICATION_ID = 1;

    private static MessageHandler defaultHandler;
    private static boolean checkDefaultHandler = true;
    private static final String TAG = AeroGearFCMMessageReceiver.class.getSimpleName();
    public static final String DEFAULT_MESSAGE_HANDLER_KEY = "DEFAULT_MESSAGE_HANDLER_KEY";

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
            final ComponentName componentName = new ComponentName(context, AeroGearFCMMessageReceiver.class);
            
            ServiceInfo si = context.getPackageManager().getServiceInfo(componentName, PackageManager.GET_META_DATA);
            Bundle metaData = si.metaData;
            
            if (metaData == null) {
                Log.d(TAG, "metaData is null. Unable to get meta data for " + componentName);
            } else {
                return metaData;
            }

        } catch (PackageManager.NameNotFoundException ex) {
            Logger.getLogger(AeroGearFCMMessageReceiver.class.getName()).log(Level.SEVERE, null, ex);

        }

        return null;
    }

}
