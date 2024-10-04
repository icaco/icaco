package io.icaco.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.jacoco.maven.AgentMojo;

import java.util.Optional;
import java.util.Properties;

import static org.apache.maven.plugins.annotations.LifecyclePhase.INITIALIZE;
import static org.apache.maven.plugins.annotations.ResolutionScope.RUNTIME;

@Mojo(name = "init-jacoco-javaagent", defaultPhase = INITIALIZE, requiresDependencyResolution = RUNTIME, threadSafe = true)
public class InitJacocoJavaagentMojo extends AgentMojo {

    @Override
    public void executeMojo() {
        super.executeMojo();
        Properties properties = getProject().getProperties();
        Optional<String> value = properties
                .values()
                .stream()
                .map(Object::toString)
                .filter(s -> s.startsWith("-javaagent:"))
                .findFirst();
        if (value.isPresent())
            properties.setProperty("jacoco-javaagent", value.get());
        else
            getLog().warn("Didn't find jacoco-javaagen property value");
    }
}
