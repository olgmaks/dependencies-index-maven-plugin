package com.depindex.plugin.service;

import com.depindex.plugin.model.DependencyInfo;
import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.util.List;

public class DependencyInfoBuilder {

    private final JarReaderService jarReader;

    public DependencyInfoBuilder(JarReaderService jarReader) {
        this.jarReader = jarReader;
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

        File jarFile = artifact.getFile();
        if (jarFile != null && jarFile.exists()) {
            info.setClasses(jarReader.extractClasses(jarFile));
            info.setJarPath(jarFile.getAbsolutePath());
        }

        return info;
    }

    public DependencyInfo jdk(String version, String type, List<String> classes) {
        DependencyInfo info = new DependencyInfo("java", "jdk", version, type, "system");
        info.setClasses(classes);
        return info;
    }
}
