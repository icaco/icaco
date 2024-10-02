package io.icaco.core.vcs;

public interface VcsCommand<T> {
    T execute();
}
