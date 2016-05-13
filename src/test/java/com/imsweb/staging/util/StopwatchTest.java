/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.staging.util;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class StopwatchTest {

    @Test
    public void testCreate() {
        Stopwatch watch = Stopwatch.create();
        Assert.assertTrue(watch.isRunning());
        watch.stop();
        Assert.assertFalse(watch.isRunning());
    }

    @Test
    public void testBasic() throws InterruptedException {
        Stopwatch watch = Stopwatch.create();
        Thread.sleep(105);
        watch.stop();

        Assert.assertTrue(watch.elapsed(TimeUnit.MILLISECONDS) > 100);
        Assert.assertEquals(0, watch.elapsed(TimeUnit.SECONDS));
        Assert.assertEquals(0, watch.elapsed(TimeUnit.MINUTES));
        Assert.assertEquals(0, watch.elapsed(TimeUnit.HOURS));
        Assert.assertEquals(0, watch.elapsed(TimeUnit.DAYS));

        Assert.assertTrue(watch.toString().endsWith(" ms"));

        watch.reset();
        Assert.assertEquals(0, watch.elapsed(TimeUnit.MILLISECONDS));
    }

}