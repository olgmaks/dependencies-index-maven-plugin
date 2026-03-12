package com.depindex.plugin.command;

import com.depindex.plugin.service.DependencyIndexService;
import com.depindex.plugin.service.IndexWriterService;
import com.depindex.plugin.service.JarReaderService;
import com.depindex.plugin.service.SearchService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

import java.util.List;
import java.util.Map;

public class SearchCommand implements Command {

    private static final String OUTPUT_DIRECTORY = ".depindex";
    private static final String OUTPUT_FILE = "dependencies.json";

    private final MavenProject project;
    private final MavenSession session;
    private final DependencyGraphBuilder dependencyGraphBuilder;
    private final int maxClasses;
    private final String className;
    private final int searchLimit;
    private final boolean reindex;
    private final Log log;

    public SearchCommand(
            MavenProject project,
            MavenSession session,
            DependencyGraphBuilder dependencyGraphBuilder,
            int maxClasses,
            String className,
            int searchLimit,
            boolean reindex,
            Log log) {
        this.project = project;
        this.session = session;
        this.dependencyGraphBuilder = dependencyGraphBuilder;
        this.maxClasses = maxClasses;
        this.className = className;
        this.searchLimit = searchLimit;
        this.reindex = reindex;
        this.log = log;
    }

    @Override
    public void execute() throws MojoExecutionException {
        SearchService searchService = new SearchService(OUTPUT_DIRECTORY, OUTPUT_FILE);

        if (!searchService.indexExists() || reindex) {
            if (reindex) {
                log.info("Reindex requested, building index...");
            } else {
                log.info("Index not found, building index...");
            }
            runIndexing();
        }

        try {
            List<Map<String, Object>> results = searchService.search(className, searchLimit);
            
            if (results.isEmpty()) {
                log.info("No classes matching '" + className + "' found in index.");
            } else {
                log.info("Found " + results.size() + " match(es):");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                System.out.println(gson.toJson(results));
            }
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
            throw new MojoExecutionException(e.getMessage());
        } catch (Exception e) {
            log.error("Search failed: " + e.getMessage());
            throw new MojoExecutionException("Search failed", e);
        }
    }

    private void runIndexing() throws MojoExecutionException {
        try {
            JarReaderService jarReader = new JarReaderService(maxClasses);
            IndexWriterService writer = new IndexWriterService(OUTPUT_DIRECTORY, OUTPUT_FILE);

            DependencyIndexService service = new DependencyIndexService(
                project,
                session,
                dependencyGraphBuilder,
                jarReader,
                writer,
                log
            );

            service.execute();
        } catch (Exception e) {
            throw new MojoExecutionException("Indexing failed", e);
        }
    }
}
