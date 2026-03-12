package com.depindex.plugin;

import com.depindex.plugin.command.ClassInfoCommand;
import com.depindex.plugin.command.Command;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "classinfo", defaultPhase = LifecyclePhase.NONE)
public class ClassInfoMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(property = "depindex.class", required = true)
    private String className;

    @Override
    public void execute() throws MojoExecutionException {
        Command command = new ClassInfoCommand(
            project,
            session,
            className,
            getLog()
        );

        try {
            command.execute();
        } catch (Exception e) {
            throw new MojoExecutionException("ClassInfo command failed", e);
        }
    }
}
