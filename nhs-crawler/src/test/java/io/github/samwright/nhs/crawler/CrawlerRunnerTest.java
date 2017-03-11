package io.github.samwright.nhs.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.frontier.Frontier;
import io.github.samwright.nhs.common.crawler.CrawlerStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrawlerRunnerTest {
    private static final int CRAWLER_COUNT = 2;
    private static final long RUNNING_URL_COUNT = 123;

    @Mock
    private Provider<Crawler> crawlerProvider;

    @Mock
    private Provider<CrawlController> controllerProvider;

    @InjectMocks
    private CrawlerRunner runner;

    private CrawlController controller, resetController;

    @Mock
    private Crawler crawler;

    @Mock
    private Frontier frontier;

    @Captor
    private ArgumentCaptor<CrawlController.WebCrawlerFactory<Crawler>> crawlerFactoryCaptor;

    private LocalDateTime halfwayThroughCrawling;

    private LocalDateTime nowWithPadding() throws InterruptedException {
        Thread.sleep(1);
        LocalDateTime now = LocalDateTime.now();
        Thread.sleep(1);
        return now;
    }

    @Before
    public void setUp() throws Exception {
        controller = mock(CrawlController.class);
        resetController = mock(CrawlController.class);
        when(crawlerProvider.get()).thenReturn(crawler);
        when(controllerProvider.get()).thenReturn(controller, resetController);
        runner.setCrawlerCount(CRAWLER_COUNT);
        when(controller.getFrontier()).thenReturn(frontier);
        when(resetController.getFrontier()).thenReturn(frontier);
        when(frontier.getNumberOfProcessedPages()).thenReturn(RUNNING_URL_COUNT);
    }

    @Test
    public void testRun() throws Exception {
        doAnswer(inv -> {
            halfwayThroughCrawling = nowWithPadding();
            assertThat(runner.getStatus().isRunning()).isTrue();
            return null;
        }).when(controller).start(crawlerFactoryCaptor.capture(), eq(CRAWLER_COUNT));

        LocalDateTime beforeCrawling = nowWithPadding();
        runner.run();
        LocalDateTime afterCrawling = nowWithPadding();

        assertThat(crawlerFactoryCaptor.getValue().newInstance()).isSameAs(crawler);

        CrawlerStatus status = runner.getStatus();
        assertThat(status.getException()).isNull();
        assertThat(status.getRunningUrlCount()).isEqualTo(RUNNING_URL_COUNT);
        assertThat(LocalDateTime.parse(status.getStartTime())).isBetween(beforeCrawling, halfwayThroughCrawling);
        assertThat(LocalDateTime.parse(status.getStopTime())).isBetween(halfwayThroughCrawling, afterCrawling);
        assertThat(status.isRunning()).isFalse();

        when(frontier.getNumberOfProcessedPages()).thenReturn(RUNNING_URL_COUNT + 1);
        assertThat(runner.getStatus().getRunningUrlCount()).isEqualTo(RUNNING_URL_COUNT + 1);
    }

    @Test
    public void testControllerIsResetForEachRun() throws Exception {
        runner.run();
        runner.run();
        verify(resetController).start(crawlerFactoryCaptor.capture(), eq(CRAWLER_COUNT));
    }

    @Test
    public void testRunWithException() throws Exception {
        RuntimeException exception = new RuntimeException();
        doThrow(exception).when(controller).start(crawlerFactoryCaptor.capture(), eq(CRAWLER_COUNT));

        LocalDateTime beforeCrawling = nowWithPadding();
        runner.run();
        LocalDateTime afterCrawling = nowWithPadding();

        CrawlerStatus status = runner.getStatus();
        assertThat(runner.getStatus().getException()).isSameAs(exception);

        LocalDateTime startTime = LocalDateTime.parse(status.getStartTime());
        assertThat(startTime).isBetween(beforeCrawling, afterCrawling);
        assertThat(LocalDateTime.parse(status.getStopTime())).isBetween(startTime, afterCrawling);
    }

    @Test
    public void testStop() throws Exception {
        runner.run();
        runner.stop();
        InOrder inOrder = inOrder(controller);
        inOrder.verify(controller).shutdown();
        inOrder.verify(controller).waitUntilFinish();
    }

    @Test
    public void testStopBeforeFirstRun() throws Exception {
        runner.stop();
        verifyNoMoreInteractions(controller);
    }

}
