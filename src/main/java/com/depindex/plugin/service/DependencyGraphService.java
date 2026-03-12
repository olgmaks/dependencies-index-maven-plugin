package com.depindex.plugin.service;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.util.ArrayList;
import java.util.List;

public class DependencyGraphService {

    private final MavenProject project;
    private final MavenSession session;
    private final DependencyGraphBuilder dependencyGraphBuilder;
    private final Log log;
    private final String localRepositoryPath;

    public DependencyGraphService(
            MavenProject project,
            MavenSession session,
            DependencyGraphBuilder dependencyGraphBuilder,
            Log log) {
        this.project = project;
        this.session = session;
        this.dependencyGraphBuilder = dependencyGraphBuilder;
        this.log = log;
        this.localRepositoryPath = session.getLocalRepository().getBasedir();
    }

    public List<Artifact> getDependencies() {
        List<Artifact> artifacts = new ArrayList<>();
        
        try {
            ProjectBuildingRequest buildingRequest = session.getProjectBuildingRequest();
            buildingRequest.setProject(project);
            DependencyNode rootNode = dependencyGraphBuilder.buildDependencyGraph(buildingRequest, null);
            
            if (rootNode != null) {
                collectDependencies(rootNode, artifacts);
            }
        } catch (DependencyGraphBuilderException e) {
            log.warn("Failed to build dependency graph: " + e.getMessage());
        }

        return artifacts;
    }

    private void collectDependencies(DependencyNode node, List<Artifact> artifacts) {
        Artifact artifact = node.getArtifact();
        if (artifact != null) {
            artifacts.add(artifact);
        }
        
        for (DependencyNode child : node.getChildren()) {
            collectDependencies(child, artifacts);
        }
    }

    public String getLocalRepositoryPath() {
        return localRepositoryPath;
    }
}
