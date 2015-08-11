package org.jboss.aerogear.android.unifiedpush.test.gcm;

import org.jboss.aerogear.android.unifiedpush.gcm.UnifiedPushConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class UnifiedPushConfigTest {

    @Test
    public void shouldSetCategories() throws Exception {
        //given
        UnifiedPushConfig config = new UnifiedPushConfig();
        List<String> categories = Arrays.asList("cat1", "cat2");

        //when
        config.setCategories(categories);

        //then

        Assert.assertEquals(categories, config.getCategories());
    }
}