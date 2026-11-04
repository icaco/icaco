package io.icaco.core.coverage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static io.icaco.core.syscmd.SysCmd.exec;
import static io.icaco.core.vcs.VcsType.Git;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.write;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChangedClassFilesTest {

    final Path repoPath = Path.of("target/changed-class-files-test").toAbsolutePath();
    final Path sourceDirectory = repoPath.resolve("src/main/java");

    @BeforeEach
    void createRepo() throws Exception {
        removeRepo();
        repoPath.toFile().mkdirs();
        exec("git -C " + repoPath, "init");
    }

    @AfterEach
    void removeRepo() throws Exception {
        deleteDirectory(repoPath.toFile());
    }

    void writeString(Path path, String data) throws Exception {
        write(path.toFile(), data, defaultCharset());
    }

    @Test
    void changedJavaFilesAsClassFiles() throws Exception {
        // Given
        writeString(sourceDirectory.resolve("io/icaco/Foo.java"), "class Foo {}");
        writeString(sourceDirectory.resolve("io/icaco/notes.txt"), "text");
        writeString(repoPath.resolve("src/test/java/io/icaco/FooTest.java"), "class FooTest {}");
        writeString(repoPath.resolve("README.md"), "text");
        // When
        List<String> classFiles = ChangedClassFiles.find(Git, repoPath, sourceDirectory);
        // Then
        assertEquals(List.of("io/icaco/Foo.class", "io/icaco/Foo$*.class"), classFiles);
    }

    @Test
    void noChanges() {
        // When
        List<String> classFiles = ChangedClassFiles.find(Git, repoPath, sourceDirectory);
        // Then
        assertEquals(List.of(), classFiles);
    }

}
