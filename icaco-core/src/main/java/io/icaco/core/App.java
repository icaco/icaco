package io.icaco.core;

import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;

public class App {
    public static void main( String[] args ) throws Exception {
        ;
        File dir = new File(".");
        System.out.println(dir.getAbsolutePath());
        System.out.println( Git.open(dir).status().call().getModified());

    }
}
