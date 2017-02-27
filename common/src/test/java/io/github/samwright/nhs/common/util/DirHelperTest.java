package io.github.samwright.nhs.common.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class DirHelperTest {
    private static final String DATA_DIR = "data", TMP_DIR = "tmp", SUB_DIR = "subDir";

    @Rule
    public TemporaryFolder rootDir = new TemporaryFolder();

    private DirHelper dirHelper = new DirHelper();

    private Path dataDir, subDataDir, tmpDir;

    @Before
    public void setUp() throws Exception {
        dataDir = rootDir.getRoot().toPath().resolve(DATA_DIR);
        subDataDir = dataDir.resolve(SUB_DIR);
        tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        dirHelper.setDataDirString(dataDir.toString());
    }

    @Test
    public void testInitWithExistingDataDir() throws Exception {
        Files.createDirectory(dataDir);
        Path fileInDir = Files.createFile(dataDir.resolve("file.txt"));
        dirHelper.init();
        assertThat(dataDir).exists().isDirectory();
        assertThat(fileInDir).exists();
    }

    @Test
    public void testUseTmpDir() throws Exception {
        dirHelper.setDataDirString("");
        dirHelper.init();
        assertThat(dirHelper.getDataDir())
                .isDirectory().exists().isEqualTo(tmpDir.resolve("io.github.samwright.nhs"));
    }

    @Test
    public void testUseRandomisedTmpDir() throws Exception {
        dirHelper.setDataDirString("");
        dirHelper.setRandomiseTmpDir(true);
        dirHelper.setDeleteOnExit(true);
        dirHelper.init();

        Path randomDataDir = dirHelper.getDataDir();
        assertThat(randomDataDir).isDirectory().exists().hasParent(tmpDir);
        dirHelper.destroy();
        assertThat(randomDataDir).doesNotExist();
    }

    @Test
    public void testCreateSubFolder() throws Exception {
        dirHelper.init();
        assertThat(dirHelper.createSubFolder(SUB_DIR))
                .exists().isDirectory().isEqualTo(subDataDir);
    }

    @Test
    public void testCreateSubFolderAgain() throws Exception {
        dirHelper.init();
        Path subDir = dirHelper.createSubFolder(SUB_DIR);
        Path fileInSubDir = Files.createFile(subDir.resolve("file.txt"));
        assertThat(dirHelper.createSubFolder(SUB_DIR)).isEqualTo(subDir);
        assertThat(fileInSubDir).exists();
    }

    @Test
    public void testDeleteOnExit() throws Exception {
        dirHelper.setDeleteOnExit(true);
        dirHelper.init();
        Files.createDirectory(subDataDir);
        Files.createFile(subDataDir.resolve("file.txt"));

        dirHelper.destroy();

        assertThat(dataDir).doesNotExist();
    }

    @Test
    public void testDoNotDeleteOnExit() throws Exception {
        dirHelper.setDeleteOnExit(false);
        dirHelper.init();
        dirHelper.destroy();
        assertThat(dataDir).exists();
    }
}
