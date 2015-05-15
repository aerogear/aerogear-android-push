package org.jboss.aerogear.android.unifiedpush.metrics;

import android.os.Bundle;
import org.jboss.aerogear.android.unifiedpush.gcm.UnifiedPushMessage;

public class UnifiedPushMetricsMessage implements MetricsMessage {

    private final String messageId;

    public UnifiedPushMetricsMessage(Bundle bundle) {
        this.messageId = bundle.getString(UnifiedPushMessage.PUSH_MESSAGE_ID);
    }

    public UnifiedPushMetricsMessage(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String getMessageId() {
        return this.messageId;
    }

}
