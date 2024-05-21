package io.icaco.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.jacoco.maven.IcacoAbstractCheckMojo;

import static org.apache.maven.plugins.annotations.LifecyclePhase.VERIFY;

@Mojo(name = "check-all-code", defaultPhase = VERIFY, threadSafe = true)
public class CheckAllCodeMojo extends IcacoAbstractCheckMojo {

}
