package io.github.samwright.nhs.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.samwright.nhs.util.DirHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Class to read and write {@link CrawledPage} on the filesystem.
 * <p/>
 * Files are named after the page's URL, replacing any non-alphabetical
 * characters with an underscore, followed by '.json'.
 */
@Component
@Slf4j
public class CrawledPageDao {
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private DirHelper dirHelper;

    private Path pagesDir;

    @PostConstruct
    public void init() throws IOException {
        pagesDir = dirHelper.createSubFolder("pages");
    }

    public synchronized void write(CrawledPage page) throws IOException {
        String filename = URLDecoder.decode(page.getUrl(), "UTF-8")
                .toLowerCase()
                .replaceAll("[^a-zA-z0-9]+", "_") + ".json";
        File resultFile = pagesDir.resolve(filename).toFile();
        mapper.writeValue(resultFile, page);
    }

    public synchronized Stream<CrawledPage> readAllPages() throws IOException {
        return Files.walk(pagesDir).filter(Files::isRegularFile).map(this::readPage);
    }

    @SneakyThrows
    private CrawledPage readPage(Path pagePath) {
        return mapper.readValue(pagePath.toFile(), CrawledPage.class);
    }
}
