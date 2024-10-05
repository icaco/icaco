package org.jacoco.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.check.IViolationsOutput;
import org.jacoco.report.check.Limit;
import org.jacoco.report.check.Rule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class IcacoAbstractCheckMojo extends AbstractJacocoMojo implements IViolationsOutput {

	protected static final String MSG_SKIPPING = "Skipping JaCoCo execution due to missing execution data file:";
	protected static final String CHECK_SUCCESS = "All coverage checks have been met.";
	protected static final String CHECK_FAILED = "Coverage checks have not been met. See log for details.";

	@Parameter(required = true)
	protected List<RuleConfiguration> rules;

	@Parameter(property = "jacoco.haltOnFailure", defaultValue = "true", required = true)
	protected boolean haltOnFailure;

	@Parameter(defaultValue = "${project.build.directory}/jacoco.exec")
	protected File dataFile;

	@Parameter
	protected List<String> includes;

	@Parameter
	protected List<String> excludes;

	protected boolean violations;

	protected boolean canCheckCoverage() {
		if (!dataFile.exists()) {
			getLog().info(MSG_SKIPPING + dataFile);
			return false;
		}
		final File classesDirectory = new File(
				getProject().getBuild().getOutputDirectory());
		if (!classesDirectory.exists()) {
			getLog().info(
					"Skipping JaCoCo execution due to missing classes directory:"
							+ classesDirectory);
			return false;
		}
		return true;
	}

	@Override
	public void executeMojo() throws MojoExecutionException {
		if (!canCheckCoverage()) {
			return;
		}
		executeCheck();
	}

	protected void executeCheck() throws MojoExecutionException {
		violations = false;

		final ReportSupport support = new ReportSupport(getLog());

		final List<Rule> checkerrules = new ArrayList<>();
		for (final RuleConfiguration r : rules) {
			checkerrules.add(r.rule);
		}
		support.addRulesChecker(checkerrules, this);

		try {
			final IReportVisitor visitor = support.initRootVisitor();
			support.loadExecutionData(dataFile);
			support.processProject(visitor, getProject(), includes, excludes);
			visitor.visitEnd();
		} catch (final IOException e) {
			throw new MojoExecutionException(
					"Error while checking code coverage: " + e.getMessage(), e);
		}
		if (violations) {
			if (this.haltOnFailure) {
				throw new MojoExecutionException(CHECK_FAILED);
			} else {
				this.getLog().warn(CHECK_FAILED);
			}
		} else {
			this.getLog().info(CHECK_SUCCESS);
		}
	}

	public void onViolation(final ICoverageNode node, final Rule rule,
			final Limit limit, final String message) {
		this.getLog().warn(message);
		violations = true;
	}

}
