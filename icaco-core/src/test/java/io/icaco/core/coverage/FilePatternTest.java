package io.icaco.core.coverage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilePatternTest {

    @Test
    void exactPath() {
        assertTrue(new FilePattern("io/icaco/Foo.class").matches("io/icaco/Foo.class"));
        assertFalse(new FilePattern("io/icaco/Foo.class").matches("io/icaco/Bar.class"));
        assertFalse(new FilePattern("io/icaco/Foo.class").matches("other/io/icaco/Foo.class"));
    }

    @Test
    void doubleStarMatchesAnyNumberOfDirectories() {
        FilePattern pattern = new FilePattern("**/*Test.class");
        assertTrue(pattern.matches("FooTest.class"));
        assertTrue(pattern.matches("io/icaco/FooTest.class"));
        assertFalse(pattern.matches("io/icaco/Foo.class"));
    }

    @Test
    void doubleStarAtEnd() {
        FilePattern pattern = new FilePattern("io/icaco/**");
        assertTrue(pattern.matches("io/icaco/Foo.class"));
        assertTrue(pattern.matches("io/icaco/core/Foo.class"));
        assertFalse(pattern.matches("io/other/Foo.class"));
    }

    @Test
    void doubleStarInMiddle() {
        FilePattern pattern = new FilePattern("io/**/Foo.class");
        assertTrue(pattern.matches("io/Foo.class"));
        assertTrue(pattern.matches("io/icaco/core/Foo.class"));
        assertFalse(pattern.matches("io/icaco/Bar.class"));
    }

    @Test
    void singleStarStaysWithinDirectory() {
        FilePattern pattern = new FilePattern("io/*.class");
        assertTrue(pattern.matches("io/Foo.class"));
        assertFalse(pattern.matches("io/icaco/Foo.class"));
    }

    @Test
    void questionMarkMatchesSingleCharacter() {
        FilePattern pattern = new FilePattern("Fo?.class");
        assertTrue(pattern.matches("Foo.class"));
        assertFalse(pattern.matches("Fooo.class"));
    }

    @Test
    void innerClasses() {
        assertTrue(new FilePattern("io/Foo$*.class").matches("io/Foo$Bar.class"));
    }

    @Test
    void trailingSlashMatchesEverythingBelow() {
        assertTrue(new FilePattern("io/icaco/").matches("io/icaco/core/Foo.class"));
    }

    @Test
    void dotIsNotAWildcard() {
        assertFalse(new FilePattern("Foo.class").matches("FooXclass"));
    }

    @Test
    void windowsSeparators() {
        assertTrue(new FilePattern("io\\icaco\\*.class").matches("io/icaco/Foo.class"));
        assertTrue(new FilePattern("io/icaco/*.class").matches("io\\icaco\\Foo.class"));
    }

}
