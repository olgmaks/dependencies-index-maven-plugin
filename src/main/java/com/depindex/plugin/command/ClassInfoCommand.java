package com.depindex.plugin.command;

import com.depindex.plugin.service.ClassInfoService;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClassInfoCommand implements Command {

    private final String className;
    private final String outputFilePath;
    private final String outputDirectory;
    private final String outputFile;
    private final Log log;

    public ClassInfoCommand(
            MavenProject project,
            MavenSession session,
            String className,
            String outputFilePath,
            String outputDirectory,
            String outputFile,
            Log log) { 
        this.className = className;
        this.outputFilePath = outputFilePath;
        this.outputDirectory = outputDirectory;
        this.outputFile = outputFile;
        this.log = log;
    }

    @Override
    public void execute() throws MojoExecutionException {
        ClassInfoService classInfoService = new ClassInfoService(outputDirectory, outputFile);

        if (!classInfoService.indexExists()) {
            log.info("Index not found. Running indexing first...");
            throw new MojoExecutionException(
                "Index not found. Please run 'mvn com.depindex:depindex-maven-plugin:index' first."
            );
        }

        try {
            String source = classInfoService.getClassSource(className);
            
            if (outputFilePath != null && !outputFilePath.isEmpty()) {
                Path path = Paths.get(outputFilePath);
                Files.createDirectories(path.getParent());
                Files.writeString(path, source);
                log.info("Class source written to: " + outputFilePath);
            } else {
                System.out.println(source);
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            throw new MojoExecutionException(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to get class info: " + e.getMessage());
            throw new MojoExecutionException("Failed to get class info", e);
        }
    }
}
