package org.jboss.aerogear.android.unifiedpush.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.jboss.aerogear.android.core.Provider;

/**
 * Provides a standard shared preferences reference
 */
public class GCMSharedPreferenceProvider implements Provider<SharedPreferences>{

    private static final String preferencesKey = GCMSharedPreferenceProvider.class.getName();
    
    @Override
    public SharedPreferences get(Object... in) {
         if (in == null || in.length != 1 || !(in[0] instanceof Context)) {
             throw new IllegalArgumentException("get requires a single Context reference");
         }
         
         Context context = (Context) in[0];
         return PreferenceManager.getDefaultSharedPreferences(context);
         
    }
    
}
