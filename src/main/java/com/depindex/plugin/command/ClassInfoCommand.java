package com.depindex.plugin.command;

import com.depindex.plugin.service.ClassInfoService;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class ClassInfoCommand implements Command {

    private static final String OUTPUT_DIRECTORY = ".depindex";
    private static final String OUTPUT_FILE = "dependencies.json";

    private final MavenProject project;
    private final MavenSession session;
    private final String className;
    private final Log log;

    public ClassInfoCommand(
            MavenProject project,
            MavenSession session,
            String className,
            Log log) { 
        this.project = project;
        this.session = session;
        this.className = className;
        this.log = log;
    }

    @Override
    public void execute() throws MojoExecutionException {
        ClassInfoService classInfoService = new ClassInfoService(OUTPUT_DIRECTORY, OUTPUT_FILE);

        if (!classInfoService.indexExists()) {
            log.info("Index not found. Running indexing first...");
            throw new MojoExecutionException(
                "Index not found. Please run search with -Ddepindex.reindex=true first."
            );
        }

        try {
            String source = classInfoService.getClassSource(className);
            
            String outputPath = classInfoService.saveClassSource(className, source);
            System.out.println(outputPath);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            throw new MojoExecutionException(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to get class info: " + e.getMessage());
            throw new MojoExecutionException("Failed to get class info", e);
        }
    }
}
