package com.depindex.plugin;

import com.depindex.plugin.command.Command;
import com.depindex.plugin.command.SearchCommand;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

import javax.inject.Inject;

@Mojo(name = "search", defaultPhase = LifecyclePhase.NONE)
public class SearchMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(property = "depindex.q", required = true)
    private String className;

    @Parameter(property = "depindex.outputDirectory", defaultValue = ".depindex")
    private String outputDirectory;

    @Parameter(property = "depindex.outputFile", defaultValue = "dependencies.json")
    private String outputFile;

    @Parameter(property = "depindex.maxClasses", defaultValue = "10000")
    private int maxClasses;

    @Parameter(property = "depindex.search.limit", defaultValue = "10")
    private int searchLimit;

    @Inject
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Override
    public void execute() throws MojoExecutionException {
        Command command = new SearchCommand(
            project,
            session,
            dependencyGraphBuilder,
            outputDirectory,
            outputFile,
            maxClasses,
            className,
            searchLimit,
            getLog()
        );

        try {
            command.execute();
        } catch (Exception e) {
            throw new MojoExecutionException("Search command failed", e);
        }
    }
}
