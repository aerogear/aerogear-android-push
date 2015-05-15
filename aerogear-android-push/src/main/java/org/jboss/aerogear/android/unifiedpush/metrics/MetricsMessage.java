package org.jboss.aerogear.android.unifiedpush.metrics;

public interface MetricsMessage {

    /**
     * A messageId. Some id that the receiver of this message can relate to.
     */
    public String getMessageId();

}
