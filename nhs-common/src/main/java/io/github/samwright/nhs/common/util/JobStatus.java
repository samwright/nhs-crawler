package io.github.samwright.nhs.common.util;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobStatus {
    private String startTime, stopTime;
    private boolean isRunning;
    private Exception exception;

    /**
     * Update the status of the job to reflect that it has been (re)started.
     */
    public void setStarted() {
        setStartTime(now()).setStopTime(null).setRunning(true).setException(null);
    }

    /**
     * Update the status of the job to reflect that it has been stopped.
     */
    public void setStopped() {
        setRunning(false).setStopTime(now());
    }

    private String now() {
        return LocalDateTime.now().toString();
    }
}
