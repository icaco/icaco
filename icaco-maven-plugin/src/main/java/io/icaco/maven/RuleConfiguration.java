package io.icaco.maven;

import org.jacoco.report.check.Limit;
import org.jacoco.report.check.Rule;

import java.util.List;

import static java.lang.String.join;
import static org.jacoco.core.analysis.ICoverageNode.ElementType;

public class RuleConfiguration {

    final Rule rule = new Rule();

    public void setElement(String element) {
        rule.setElement(ElementType.valueOf(element));
    }

    public void setIncludes(List<String> includes) {
        rule.setIncludes(join(":", includes));
    }

    public void setExcludes(List<String> excludes) {
        rule.setExcludes(join(":", excludes));
    }

    public void setLimits(List<Limit> limits) {
        rule.setLimits(limits);
    }

}
