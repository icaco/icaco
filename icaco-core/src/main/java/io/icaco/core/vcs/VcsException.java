package io.icaco.core.vcs;

import io.icaco.core.syscmd.SysCmdResult;

public class VcsException extends RuntimeException {

    public VcsException(Exception e) {
        super(e);
    }

    public VcsException(String msg) {
        super(msg);
    }

    public VcsException(SysCmdResult result) {
        super("Git command '" + result.getCommand() + "' has exit code " + result.getExitCode());
    }

}
