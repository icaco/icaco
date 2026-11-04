package io.icaco.core.coverage;

import io.icaco.core.vcs.VcsChangesCommand;
import io.icaco.core.vcs.VcsType;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.io.File.separatorChar;
import static java.util.stream.Collectors.toList;

/**
 * Finds the class files of the Java source files that are changed according to the VCS,
 * as patterns relative to the classes directory. A changed {@code io/icaco/Foo.java} gives
 * {@code io/icaco/Foo.class} and {@code io/icaco/Foo$*.class}, which matches its inner and
 * anonymous classes.
 */
public class ChangedClassFiles {

    public static List<String> find(VcsType vcsType, Path basePath, Path sourceDirectory) {
        Path sourcePath = sourceDirectory.toAbsolutePath();
        return VcsChangesCommand.create(vcsType, basePath)
                .execute()
                .stream()
                .filter(path -> path.startsWith(sourcePath))
                .map(path -> sourcePath.relativize(path).toString().replace(separatorChar, '/'))
                .filter(s -> s.endsWith(".java"))
                .map(s -> s.substring(0, s.length() - ".java".length()))
                .flatMap(s -> Stream.of(s + ".class", s + "$*.class"))
                .collect(toList());
    }

}
