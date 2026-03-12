package com.depindex.plugin;

import com.depindex.plugin.command.Command;
import com.depindex.plugin.command.IndexCommand;
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

@Mojo(name = "index", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class DepIndexMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(property = "depindex.outputDirectory", defaultValue = ".depindex")
    private String outputDirectory;

    @Parameter(property = "depindex.outputFile", defaultValue = "dependencies.json")
    private String outputFile;

    @Parameter(property = "depindex.maxClasses", defaultValue = "10000")
    private int maxClasses;

    @Inject
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Override
    public void execute() throws MojoExecutionException {
        Command command = new IndexCommand(
            project,
            session,
            dependencyGraphBuilder,
            outputDirectory,
            outputFile,
            maxClasses,
            getLog()
        );

        try {
            command.execute();
        } catch (Exception e) {
            throw new MojoExecutionException("Index command failed", e);
        }
    }
}
