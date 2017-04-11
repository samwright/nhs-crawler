package io.github.samwright.nhs.pages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.samwright.nhs.common.pages.Page;
import io.github.samwright.nhs.common.pages.PageBatch;
import io.github.samwright.nhs.common.util.DirHelper;
import lombok.SneakyThrows;

/**
 * Class to read and write {@link Page} on the filesystem.
 * <p/>
 * Files are named after the page's URL, replacing any non-alphabetical
 * characters with an underscore, followed by '.json'.
 */
@RestController
@RequestMapping("/api/page")
public class PagesRestController {
    private static final String TEMP_FILE_PREFIX = "temp-";
    private static final int BATCH_SIZE = 20;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private DirHelper dirHelper;

    private Path pagesDir;

    @PostConstruct
    public void init() throws IOException {
        pagesDir = dirHelper.createSubFolder("pages");
    }

    @PutMapping
    public void create(@RequestBody Page page) throws IOException {
        String filename = getFilename(page);
        Path resultFile = pagesDir.resolve(TEMP_FILE_PREFIX + filename);
        mapper.writeValue(resultFile.toFile(), page);
        Files.move(resultFile, pagesDir.resolve(filename), StandardCopyOption.ATOMIC_MOVE);
    }

    private String getFilename(Page page) throws UnsupportedEncodingException {
        return URLDecoder.decode(page.getUrl(), "UTF-8")
                .toLowerCase()
                .replaceAll("[^a-zA-z0-9]+", "_") + ".json";
    }

    @GetMapping
    public PageBatch read(@RequestParam Optional<String> nextResult,
                          @RequestParam Optional<Integer> requestBatchSize) throws IOException {
        int batchSize = requestBatchSize.map(s -> Math.min(BATCH_SIZE, s)).orElse(BATCH_SIZE);
        AtomicBoolean pageStarted = new AtomicBoolean(!nextResult.isPresent());
        List<Page> pages = Files.walk(pagesDir)
                .filter(Files::isRegularFile)
                .filter(f -> !f.getFileName().toString().startsWith(TEMP_FILE_PREFIX))
                .filter(f -> {
                    if (!pageStarted.get() && f.getFileName().toString().equals(nextResult.get())) {
                        pageStarted.set(true);
                    }
                    return pageStarted.get();
                })
                .limit(batchSize + 1)
                .map(this::readPage)
                .collect(Collectors.toList());

        PageBatch batch = new PageBatch();
        if (pages.size() > batchSize) {
            Page next = pages.remove(pages.size() - 1);
            batch.setNextResult(getFilename(next));
        } else {
            batch.setLast(true);
        }
        return batch.setPages(pages);
    }

    @SneakyThrows
    private Page readPage(Path pagePath) {
        return mapper.readValue(pagePath.toFile(), Page.class);
    }
}
