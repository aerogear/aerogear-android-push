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
package org.jboss.aerogear.android.unifiedpush.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * This is an Android Service which listens for InstanceID messages from Google's GCM servicers.
 * 
 * See https://developers.google.com/instance-id/guides/android-implementation#refresh_tokens for official docs
 * 
 */
public class UnifiedPushInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    /**
     * This method is called when the Google Services have instructed us to 
     * refresh out token states.
     */
    public void onTokenRefresh() {
        super.onTokenRefresh();
    }
    
    
    
}
