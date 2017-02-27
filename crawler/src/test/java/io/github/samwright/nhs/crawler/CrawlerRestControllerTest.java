package io.github.samwright.nhs.crawler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrawlerRestControllerTest {
    @Mock
    private CrawlerRunner runner;

    @InjectMocks
    private CrawlerRestController controller;

    private CrawlerStatus status = new CrawlerStatus();

    private int runCount = 0, shutdownCount = 0;

    @Before
    public void setUp() throws Exception {
        status.setStartTime("a long time ago");
        when(runner.getStatus()).thenReturn(status);
        doAnswer(inv -> ++runCount).when(runner).run();
        doAnswer(inv -> ++shutdownCount).when(runner).stop();
    }

    @After
    public void tearDown() throws Exception {
        controller.shutdown();
    }

    @Test
    public void testStatus() throws Exception {
        assertThat(controller.status()).isSameAs(status);
    }

    @Test
    public void testStart() throws Exception {
        assertThat(controller.start()).isEqualTo("crawler started");
        Thread.sleep(100);
        assertThat(runCount).isEqualTo(1);
    }

    @Test
    public void testAlreadyStarted() throws Exception {
        status.setRunning(true);
        assertThat(controller.start()).isEqualTo("already running");
        Thread.sleep(100);
        assertThat(runCount).isEqualTo(0);
    }

    @Test
    public void testStop() throws Exception {
        status.setRunning(true);
        assertThat(controller.stop()).isEqualTo("stopping now");
        Thread.sleep(100);
        assertThat(shutdownCount).isEqualTo(1);
    }

    @Test
    public void testAlreadyStopped() throws Exception {
        assertThat(controller.stop()).isEqualTo("already stopped");
        Thread.sleep(100);
        assertThat(shutdownCount).isEqualTo(0);
    }
}
