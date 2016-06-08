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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import org.jboss.aerogear.android.core.ConfigurationProvider;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushConfiguration;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushJsonConfiguration;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushJsonConfigurationProvider;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushConfigurationProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the factory and accessors for PushRegistrars
 */
public class RegistrarManager {

    private static final Map<String, PushRegistrar> REGISTRARS = new HashMap<>();

    private static final List<MessageHandler> MAIN_THREAD_HANDLERS = new ArrayList<>();
    private static final List<MessageHandler> BACKGROUND_THREAD_HANDLERS = new ArrayList<>();

    private static final Map<Class<? extends PushConfiguration<?>>, ConfigurationProvider<?>> CONFIGURATION_PROVIDER_MAP = new HashMap<>();

    private static final OnPushRegistrarCreatedListener ON_PUSH_REGISTRAR_CREATED_LISTENER = new OnPushRegistrarCreatedListener() {

        @Override
        public void onPushRegistrarCreated(PushConfiguration<?> configuration, PushRegistrar registrar) {
            REGISTRARS.put(configuration.getName(), registrar);
        }
    };

    static {
        RegistrarManager.registerConfigurationProvider(AeroGearFCMPushConfiguration.class,
                new AeroGearFCMPushConfigurationProvider());
        RegistrarManager.registerConfigurationProvider(AeroGearFCMPushJsonConfiguration.class,
                new AeroGearFCMPushJsonConfigurationProvider());
    }

    /**
     * 
     * This will add a new Configuration that this Manager can build
     * Configurations for.
     * 
     * @param <CFG> the actual Configuration type
     * @param configurationClass the class of configuration to be registered
     * @param provider the instance which will provide the configuration.
     */
    public static <CFG extends PushConfiguration<CFG>> void registerConfigurationProvider(
            Class<CFG> configurationClass, ConfigurationProvider<CFG> provider) {
        CONFIGURATION_PROVIDER_MAP.put(configurationClass, provider);
    }

    /**
     * Begins a new fluent configuration stanza.
     * 
     * @param <CFG> the Configuration type.
     * @param name an identifier which will be used to fetch the
     *            PushRegistrar after configuration is finished.
     * @param pushConfigurationClass the class of the configuration
     *            type.
     * 
     * @return a {@link PushConfiguration} which can be used to build a
     *         AuthenticationModule object.
     */
    public static <CFG extends PushConfiguration<CFG>> CFG config(String name, Class<CFG> pushConfigurationClass) {

        @SuppressWarnings("unchecked")
        ConfigurationProvider<? extends PushConfiguration<CFG>> provider = (ConfigurationProvider<? extends PushConfiguration<CFG>>) CONFIGURATION_PROVIDER_MAP
                .get(pushConfigurationClass);

        if (provider == null) {
            throw new IllegalArgumentException("Configuration not registered!");
        }

        return provider.newConfiguration()
                .setName(name)
                .addOnPushRegistrarCreatedListener(ON_PUSH_REGISTRAR_CREATED_LISTENER);

    }

    /**
     * Fetches a named registrar
     * 
     * @param name the name of the {@link PushRegistrar} given in {@link RegistrarManager#config(java.lang.String, java.lang.Class)
     * }
     * 
     * @return the named {@link PushRegistrar} or null
     */
    public static PushRegistrar getRegistrar(String name) {
        return REGISTRARS.get(name);
    }

    /**
     * 
     * When a push message is received, all main thread handlers will be
     * notified on the main(UI) thread. This is very convenient for Activities
     * and Fragments.
     * 
     * @param handler a handler to added to the list of handlers to be notified.
     */
    public static void registerMainThreadHandler(MessageHandler handler) {
        MAIN_THREAD_HANDLERS.add(handler);
    }

    /**
     * 
     * When a push message is received, all background thread handlers will be
     * notified on a non UI thread. This should be used by classes which need to
     * update internal state or preform some action which doesn't change the UI.
     * 
     * @param handler a handler to added to the list of handlers to be notified.
     */
    public static void registerBackgroundThreadHandler(MessageHandler handler) {
        BACKGROUND_THREAD_HANDLERS.add(handler);
    }

    /**
     * 
     * This will remove the given handler from the collection of main thread
     * handlers. This MUST be called when a Fragment or activity is backgrounded
     * via onPause.
     * 
     * @param handler a new handler
     */
    public static void unregisterMainThreadHandler(MessageHandler handler) {
        MAIN_THREAD_HANDLERS.remove(handler);
    }

    /**
     * 
     * This will remove the given handler from the collection of background
     * thread handlers.
     * 
     * @param handler a new handler
     */
    public static void unregisterBackgroundThreadHandler(MessageHandler handler) {
        BACKGROUND_THREAD_HANDLERS.remove(handler);
    }

    /**
     * 
     * This will deliver an message to all registered handlers. 
     * 
     * @param context the message context
     * @param message the message to pass
     * @param defaultHandler a default handler is a handler which will be called
     *            if there are no other handlers registered. May be null
     */
    public static void notifyHandlers(final Context context, final Bundle message, final MessageHandler defaultHandler) {

        if (BACKGROUND_THREAD_HANDLERS.isEmpty() && MAIN_THREAD_HANDLERS.isEmpty()
                && defaultHandler != null) {
            new Thread(new Runnable() {
                public void run() {
                    defaultHandler.onMessage(context, message);
                }
            }).start();
        }

        for (final MessageHandler handler : BACKGROUND_THREAD_HANDLERS) {
            new Thread(new Runnable() {
                public void run() {
                    handler.onMessage(context, message);
                }
            }).start();
        }

        Looper main = Looper.getMainLooper();

        for (final MessageHandler handler : MAIN_THREAD_HANDLERS) {
            new Handler(main).post(new Runnable() {
                @Override
                public void run() {
                    handler.onMessage(context, message);
                }
            });
        }
    }

    /**
     * 
     * This will deliver an message to all registered handlers.
     * 
     * @param context the message context
     * @param message the message to pass
     */
    protected static void notifyHandlers(final Context context, final Bundle message) {
        notifyHandlers(context, message, null);
    }

}
