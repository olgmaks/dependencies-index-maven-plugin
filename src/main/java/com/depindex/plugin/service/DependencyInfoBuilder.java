package com.depindex.plugin.service;

import com.depindex.plugin.model.DependencyInfo;
import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.util.List;

public class DependencyInfoBuilder {

    private final JarReaderService jarReader;
    private final String localRepositoryPath;

    public DependencyInfoBuilder(JarReaderService jarReader, String localRepositoryPath) {
        this.jarReader = jarReader;
        this.localRepositoryPath = localRepositoryPath;
    }

    public DependencyInfo fromArtifact(Artifact artifact) {
        String version = artifact.getVersion() != null ? artifact.getVersion() : "unknown";
        String scope = artifact.getScope() != null ? artifact.getScope() : "compile";
        
        DependencyInfo info = new DependencyInfo(
            artifact.getGroupId(),
            artifact.getArtifactId(),
            version,
            artifact.getType() != null ? artifact.getType() : "jar",
            scope
        );

        File jarFile = resolveJarFile(artifact);
        if (jarFile != null && jarFile.exists()) {
            info.setClasses(jarReader.extractClasses(jarFile));
            info.setJarPath(jarFile.getAbsolutePath());
        }

        return info;
    }

    private File resolveJarFile(Artifact artifact) {
        String type = artifact.getType() != null ? artifact.getType() : "jar";
        String classifier = artifact.getClassifier();
        
        String path = artifact.getGroupId().replace('.', '/') + "/" 
            + artifact.getArtifactId() + "/" 
            + artifact.getVersion() + "/"
            + artifact.getArtifactId() + "-" + artifact.getVersion();
        
        if (classifier != null && !classifier.isEmpty()) {
            path += "-" + classifier;
        }
        path += "." + type;
        
        return new File(localRepositoryPath, path);
    }

    public DependencyInfo jdk(String version, String type, List<String> classes) {
        DependencyInfo info = new DependencyInfo("java", "jdk", version, type, "system");
        info.setClasses(classes);
        return info;
    }
}
