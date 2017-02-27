package io.github.samwright.nhs.util;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class JobStatusTest {
    private JobStatus status = new JobStatus();
    private IOException exception = new IOException();

    @Before
    public void setUp() throws Exception {
        status.setStartTime("friday");
        status.setStopTime("saturday");
        status.setRunning(false);
        status.setException(exception);
    }

    @Test
    public void testSetStarted() throws Exception {
        LocalDateTime before = nowWithPadding();
        status.setStarted();
        LocalDateTime after = nowWithPadding();

        assertThat(LocalDateTime.parse(status.getStartTime())).isBetween(before, after);
        assertThat(status.getStopTime()).isNull();
        assertThat(status.isRunning()).isTrue();
        assertThat(status.getException()).isNull();
    }

    @Test
    public void testSetStopped() throws Exception {
        LocalDateTime before = nowWithPadding();
        status.setStarted();
        LocalDateTime halfWayThrough = nowWithPadding();
        status.setStopped();
        LocalDateTime after = nowWithPadding();

        assertThat(LocalDateTime.parse(status.getStartTime())).isBetween(before, halfWayThrough);
        assertThat(LocalDateTime.parse(status.getStopTime())).isBetween(halfWayThrough, after);
        assertThat(status.isRunning()).isFalse();
        assertThat(status.getException()).isNull();
    }

    private LocalDateTime nowWithPadding() throws InterruptedException {
        Thread.sleep(1);
        LocalDateTime now = LocalDateTime.now();
        Thread.sleep(1);
        return now;
    }
}
