package io.github.samwright.nhs.util;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class DirHelper {
    private static final String TEMP_DIR_NAME = "io.github.samwright.nhs";

    @Value("${dataDir:}") @Setter
    private String dataDirString;

    @Value("${randomiseTmpDir:false}") @Setter
    private boolean randomiseTmpDir;

    @Value("${deleteOnExit:false}") @Setter
    private boolean deleteOnExit;

    @Getter
    private Path dataDir;

    @PostConstruct
    public void init() throws IOException {
        if (dataDirString.isEmpty()) {
            if (randomiseTmpDir) {
                dataDir = Files.createTempDirectory(TEMP_DIR_NAME);
            } else {
                dataDir = Paths.get(System.getProperty("java.io.tmpdir"), TEMP_DIR_NAME);
            }
        } else {
            dataDir = Paths.get(dataDirString);
        }
        Files.createDirectories(dataDir);
        log.info("Files will be stored in {}", dataDir);
    }

    @PreDestroy
    public void destroy() {
        if (deleteOnExit) {
            log.info("Deleting files in {} because deleteOnExit=true", dataDir);
            FileUtils.deleteQuietly(dataDir.toFile());
        }
    }

    public Path createSubFolder(String subFolderName) throws IOException {
        Path subFolder = dataDir.resolve(subFolderName);
        if (Files.notExists(subFolder)) {
            Files.createDirectory(subFolder);
        }
        return subFolder;
    }
}
