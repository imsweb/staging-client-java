package com.imsweb.staging.util;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StopwatchTest {

    @Test
    public void testStopwatch() {
        Stopwatch sw = Stopwatch.create();

        assertThat(sw.isRunning()).isTrue();
        sw.stop();
        assertThat(sw.isRunning()).isFalse();

        sw = Stopwatch.create();
        sw.reset();
        assertThat(sw.isRunning()).isFalse();

        sw = Stopwatch.create();
        assertThat(sw.elapsed(TimeUnit.NANOSECONDS)).isPositive();
        assertThat(sw.toString()).isNotEmpty();
    }

}