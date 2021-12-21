package com.imsweb.staging.util;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    public void testExceptions() {
        final Stopwatch sw = Stopwatch.create();
        assertThatThrownBy(sw::start).isInstanceOf(IllegalStateException.class).hasMessageContaining("This stopwatch is already running");

        sw.stop();
        assertThatThrownBy(sw::stop).isInstanceOf(IllegalStateException.class).hasMessageContaining("This stopwatch is already stopped");
    }

}