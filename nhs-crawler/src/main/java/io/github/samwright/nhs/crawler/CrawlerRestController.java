package io.github.samwright.nhs.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.samwright.nhs.common.crawler.CrawlerStatus;

@RestController
@RequestMapping("/api/crawler")
public class CrawlerRestController {
    private final ExecutorService runExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService shutdownExecutor = Executors.newSingleThreadExecutor();

    @Autowired
    private CrawlerRunner runner;

    @RequestMapping("/start")
    public synchronized String start() {
        if (runner.getStatus().isRunning()) {
            return "already running";
        } else {
            runExecutor.submit(runner::run);
            return "crawler started";
        }
    }

    @RequestMapping("/stop")
    public synchronized String stop() {
        if (runner.getStatus().isRunning()) {
            shutdownExecutor.execute(runner::stop);
            return "stopping now";
        } else {
            return "already stopped";
        }
    }

    @RequestMapping("/status")
    public CrawlerStatus status() {
        return runner.getStatus();
    }

    @PreDestroy
    public void shutdown()
    {
        runExecutor.shutdown();
        shutdownExecutor.shutdown();
    }
}
