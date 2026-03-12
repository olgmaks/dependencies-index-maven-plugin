package com.depindex.plugin.service;

import com.depindex.plugin.model.DependencyInfo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DependencyIndexService {

    private final MavenProject project;
    private final MavenSession session;
    private final DependencyGraphBuilder dependencyGraphBuilder;
    private final JarReaderService jarReader;
    private final IndexWriterService writer;
    private final Log log;
    private final DependencyInfoBuilder infoBuilder;

    public DependencyIndexService(
            MavenProject project,
            MavenSession session,
            DependencyGraphBuilder dependencyGraphBuilder,
            JarReaderService jarReader,
            IndexWriterService writer,
            Log log) {
        this.project = project;
        this.session = session;
        this.dependencyGraphBuilder = dependencyGraphBuilder;
        this.jarReader = jarReader;
        this.writer = writer;
        this.log = log;
        this.infoBuilder = new DependencyInfoBuilder(jarReader);
    }

    public void execute() {
        log.info("Indexing dependencies...");

        List<DependencyInfo> allDependencies = new ArrayList<>();
        
        allDependencies.addAll(getCurrentProjectClasses());
        allDependencies.addAll(getMavenDependencies());
        allDependencies.addAll(getJdkClasses());
        
        try {
            writer.write(allDependencies);
            log.info("Indexed " + allDependencies.size() + " dependencies to " + writer.getOutputFile().getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to write dependencies index", e);
        }
    }

    private List<DependencyInfo> getCurrentProjectClasses() {
        List<DependencyInfo> projectClasses = new ArrayList<>();
        
        if (project.getBuild() == null) {
            return projectClasses;
        }
        
        String outputDirPath = project.getBuild().getOutputDirectory();
        if (outputDirPath != null) {
            File outputFile = new File(outputDirPath);
            if (outputFile.exists() && outputFile.isDirectory()) {
                log.info("Indexing current project classes...");
                List<String> classes = jarReader.extractClassesFromDir(outputFile);
                if (!classes.isEmpty()) {
                    DependencyInfo info = new DependencyInfo(
                        project.getGroupId(),
                        project.getArtifactId(),
                        project.getVersion(),
                        "jar",
                        "project"
                    );
                    info.setClasses(classes);
                    info.setJarPath(outputFile.getAbsolutePath());
                    projectClasses.add(info);
                }
            }
        }
        
        return projectClasses;
    }

    private List<DependencyInfo> getMavenDependencies() {
        DependencyGraphService graphService = new DependencyGraphService(
            project, session, dependencyGraphBuilder, log);
        List<Artifact> artifacts = graphService.getDependencies();
        
        List<DependencyInfo> dependencies = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            dependencies.add(infoBuilder.fromArtifact(artifact));
        }
        return dependencies;
    }

    private List<DependencyInfo> getJdkClasses() {
        List<DependencyInfo> jdkClasses = new ArrayList<>();
        String javaHome = getJavaHome();
        
        File rtJar = new File(javaHome, "lib/rt.jar");
        if (rtJar.exists()) {
            log.info("Indexing JDK classes from rt.jar...");
            List<String> classes = jarReader.extractClasses(rtJar);
            jdkClasses.add(infoBuilder.jdk(System.getProperty("java.version"), "jar", classes));
            return jdkClasses;
        }
        
        File jmodDir = new File(javaHome, "jmods");
        if (jmodDir.exists() && jmodDir.isDirectory()) {
            log.info("Indexing JDK classes from jmods...");
            File[] jmodFiles = jmodDir.listFiles((d, n) -> n.startsWith("java.") && n.endsWith(".jmod"));
            
            if (jmodFiles != null) {
                List<String> allClasses = new ArrayList<>();
                for (File jmodFile : jmodFiles) {
                    allClasses.addAll(jarReader.extractClasses(jmodFile));
                }
                jdkClasses.add(infoBuilder.jdk(System.getProperty("java.version"), "jmod", allClasses));
            }
        }
        
        return jdkClasses;
    }

    private String getJavaHome() {
        String javaHome = System.getProperty("java.home");
        File contentsHome = new File(javaHome, "Contents/Home");
        if (contentsHome.exists()) {
            return contentsHome.getAbsolutePath();
        }
        return javaHome;
    }
}
