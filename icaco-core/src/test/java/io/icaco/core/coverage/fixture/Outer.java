package io.icaco.core.coverage.fixture;

/**
 * Compiles to Outer.class, Outer$Inner.class and Outer$1.class, for testing inner class patterns.
 */
public class Outer {

    public static class Inner {
        public int value() {
            return 1;
        }
    }

    public Runnable anonymous() {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("run");
            }
        };
    }

}
