package com.depindex.plugin.command;

import com.depindex.plugin.service.DependencyIndexService;
import com.depindex.plugin.service.IndexWriterService;
import com.depindex.plugin.service.JarReaderService;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

public class IndexCommand implements Command {

    private final MavenProject project;
    private final MavenSession session;
    private final DependencyGraphBuilder dependencyGraphBuilder;
    private final String outputDirectory;
    private final String outputFile;
    private final int maxClasses;
    private final Log log;

    public IndexCommand(
            MavenProject project,
            MavenSession session,
            DependencyGraphBuilder dependencyGraphBuilder,
            String outputDirectory,
            String outputFile,
            int maxClasses,
            Log log) {
        this.project = project;
        this.session = session;
        this.dependencyGraphBuilder = dependencyGraphBuilder;
        this.outputDirectory = outputDirectory;
        this.outputFile = outputFile;
        this.maxClasses = maxClasses;
        this.log = log;
    }

    @Override
    public void execute() {
        JarReaderService jarReader = new JarReaderService(maxClasses);
        IndexWriterService writer = new IndexWriterService(outputDirectory, outputFile);

        DependencyIndexService service = new DependencyIndexService(
            project,
            session,
            dependencyGraphBuilder,
            jarReader,
            writer,
            log
        );

        service.execute();
    }
}
