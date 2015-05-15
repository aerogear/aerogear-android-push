package org.jboss.aerogear.android.unifiedpush.metrics;

import org.jboss.aerogear.android.core.Callback;

public interface MetricsSender<T extends MetricsMessage> {

    void sendMetrics(T message, Callback<T> callback);

}
