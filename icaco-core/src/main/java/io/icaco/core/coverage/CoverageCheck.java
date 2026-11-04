package io.icaco.core.coverage;

import lombok.Builder;
import lombok.NonNull;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.check.Rule;
import org.jacoco.report.check.RulesChecker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Checks code coverage from a JaCoCo execution data file against JaCoCo rules.
 * An empty {@code includes} list includes every class file.
 */
@Builder
public class CoverageCheck {

    @NonNull File execFile;
    @NonNull File classesDirectory;
    @NonNull List<Rule> rules;
    @Builder.Default String bundleName = "";
    @Builder.Default List<String> includes = List.of();
    @Builder.Default List<String> excludes = List.of();

    public CoverageCheckResult execute() throws IOException {
        ExecFileLoader loader = new ExecFileLoader();
        loader.load(execFile);
        IBundleCoverage bundle = analyze(loader);
        List<String> violations = new ArrayList<>();
        RulesChecker checker = new RulesChecker();
        checker.setRules(rules);
        IReportVisitor visitor = checker.createVisitor((node, rule, limit, message) -> violations.add(message));
        visitor.visitInfo(loader.getSessionInfoStore().getInfos(), loader.getExecutionDataStore().getContents());
        // The rules checker never reads source files, so no locator is needed
        visitor.visitBundle(bundle, null);
        visitor.visitEnd();
        return new CoverageCheckResult(bundle.getName(), bundle.getClassCounter().getTotalCount(), violations);
    }

    IBundleCoverage analyze(ExecFileLoader loader) throws IOException {
        CoverageBuilder builder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(loader.getExecutionDataStore(), builder);
        for (File file : classFiles())
            analyzer.analyzeAll(file);
        return builder.getBundle(bundleName);
    }

    List<File> classFiles() throws IOException {
        Path root = classesDirectory.toPath();
        List<FilePattern> includePatterns = patterns(includes.isEmpty() ? List.of("**") : includes);
        List<FilePattern> excludePatterns = patterns(excludes);
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String relative = root.relativize(path).toString();
                        return matchesAny(includePatterns, relative) && !matchesAny(excludePatterns, relative);
                    })
                    .sorted()
                    .map(Path::toFile)
                    .collect(toList());
        }
    }

    static List<FilePattern> patterns(List<String> patterns) {
        return patterns.stream().map(FilePattern::new).collect(toList());
    }

    static boolean matchesAny(List<FilePattern> patterns, String path) {
        return patterns.stream().anyMatch(p -> p.matches(path));
    }

}
