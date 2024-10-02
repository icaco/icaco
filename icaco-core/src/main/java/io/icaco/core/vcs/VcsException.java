package io.icaco.core.vcs;

public class VcsException extends RuntimeException {

    public VcsException(Exception e) {
        super(e);
    }

    public VcsException(String msg) {
        super(msg);
    }

}
