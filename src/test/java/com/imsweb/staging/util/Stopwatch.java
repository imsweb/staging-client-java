/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.staging.util;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Stopwatch class based on Guava implementation
 */
public final class Stopwatch {

    private boolean _isRunning;
    private long _elapsedNanos;
    private long _startTick;

    /**
     * Creates (and starts) a new stopwatch
     */
    public static Stopwatch create() {
        return new Stopwatch().start();
    }

    Stopwatch() {
    }

    /**
     * Returns {@code true} if {@link #start()} has been called on this stopwatch, and {@link #stop()}
     * has not been called since the last call to {@code
     * start()}.
     */
    public boolean isRunning() {
        return _isRunning;
    }

    /**
     * Starts the stopwatch.
     * @return this {@code Stopwatch} instance
     * @throws IllegalStateException if the stopwatch is already running.
     */
    public Stopwatch start() {
        if (isRunning())
            throw new IllegalStateException("This stopwatch is already running.");
        _isRunning = true;
        _startTick = System.nanoTime();
        return this;
    }

    /**
     * Stops the stopwatch. Future reads will return the fixed duration that had elapsed up to this
     * point.
     * @return this {@code Stopwatch} instance
     * @throws IllegalStateException if the stopwatch is already stopped.
     */
    public Stopwatch stop() {
        long tick = System.nanoTime();
        if (!isRunning())
            throw new IllegalStateException("This stopwatch is already stopped.");
        _isRunning = false;
        _elapsedNanos += tick - _startTick;
        return this;
    }

    /**
     * Sets the elapsed time for this stopwatch to zero, and places it in a stopped state.
     * @return this {@code Stopwatch} instance
     */
    public Stopwatch reset() {
        _elapsedNanos = 0;
        _isRunning = false;
        return this;
    }

    private long elapsedNanos() {
        return _isRunning ? System.nanoTime() - _startTick + _elapsedNanos : _elapsedNanos;
    }

    /**
     * Returns the current elapsed time shown on this stopwatch, expressed in the desired time unit,
     * with any fraction rounded down.
     * <p>
     * <p>Note that the overhead of measurement can be more than a microsecond, so it is generally not
     * useful to specify {@link TimeUnit#NANOSECONDS} precision here.
     */
    public long elapsed(TimeUnit desiredUnit) {
        return desiredUnit.convert(elapsedNanos(), NANOSECONDS);
    }

    /**
     * Returns a string representation of the current elapsed time.
     */
    @Override
    public String toString() {
        long nanos = elapsedNanos();

        TimeUnit unit = chooseUnit(nanos);
        double value = (double)nanos / NANOSECONDS.convert(1, unit);

        // Too bad this functionality is not exposed as a regular method call
        return String.format(Locale.ROOT, "%.4g", value) + " " + abbreviate(unit);
    }

    private static TimeUnit chooseUnit(long nanos) {
        if (DAYS.convert(nanos, NANOSECONDS) > 0)
            return DAYS;
        if (HOURS.convert(nanos, NANOSECONDS) > 0)
            return HOURS;
        if (MINUTES.convert(nanos, NANOSECONDS) > 0)
            return MINUTES;
        if (SECONDS.convert(nanos, NANOSECONDS) > 0)
            return SECONDS;
        if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0)
            return MILLISECONDS;
        if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0)
            return MICROSECONDS;

        return NANOSECONDS;
    }

    private static String abbreviate(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "\u03bcs"; // μs
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new AssertionError();
        }
    }

}