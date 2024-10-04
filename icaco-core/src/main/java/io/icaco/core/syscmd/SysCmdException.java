package io.icaco.core.syscmd;

public class SysCmdException extends RuntimeException {
    public SysCmdException(Exception cause) {
        super(cause.getMessage(), cause);
    }
}
