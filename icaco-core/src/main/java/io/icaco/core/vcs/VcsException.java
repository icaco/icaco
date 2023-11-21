package io.icaco.core.vcs;

class VcsException extends RuntimeException {

    VcsException(Exception e) {
        super(e);
    }

    VcsException(String msg) {
        super(msg);
    }

}
