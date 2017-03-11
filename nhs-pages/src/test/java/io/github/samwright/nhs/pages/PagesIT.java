package io.github.samwright.nhs.pages;

import io.github.samwright.nhs.common.pages.Page;
import io.github.samwright.nhs.common.pages.PageBatch;
import io.github.samwright.nhs.common.pages.PagesClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = {"randomiseTmpDir=true", "deleteOnExit=true",
                "eureka.client.enabled=false", "feign.hystrix.enabled=false",
                "nhs-pages-app.ribbon.listOfServers:localhost:${server.port}"},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(clients = PagesClient.class)
@DirtiesContext
public class PagesIT {
    @Autowired
    private PagesClient client;

    @Test
    public void testWriteAndRead() throws Exception {
        PageBatch emptyBatch = client.read();
        assertThat(emptyBatch.isLast()).isTrue();
        assertThat(emptyBatch.getNextResult()).isNull();
        assertThat(emptyBatch.getPages()).isEmpty();

        Page page = new Page().setContent("content").setUrl("url").setTitle("title");
        client.create(page);

        PageBatch batch = client.read();
        assertThat(batch.isLast()).isTrue();
        assertThat(batch.getNextResult()).isNull();
        assertThat(batch.getPages()).containsOnly(page);
    }
}
