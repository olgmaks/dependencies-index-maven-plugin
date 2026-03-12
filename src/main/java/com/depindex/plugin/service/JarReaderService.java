package com.depindex.plugin.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JarReaderService {

    public JarReaderService() {
    }

    public List<String> extractClasses(File jarFile) {
        List<String> classes = new ArrayList<>();

        if (jarFile == null || !jarFile.exists()) {
            return classes;
        }

        if (jarFile.isDirectory()) {
            return extractClassesFromDir(jarFile);
        }

        try {
            Path jarPath = jarFile.toPath();
            FileSystem fs = FileSystems.newFileSystem(jarPath, JarReaderService.class.getClassLoader());
            
            Files.walk(fs.getPath("/"))
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .filter(this::isPublicClass)
                .map(this::toClassName)
                .forEach(classes::add);
            
            fs.close();
        } catch (IOException e) {
            // Silently ignore unreadable JARs
        }

        return classes;
    }

    public List<String> extractClassesFromDir(File dir) {
        List<String> classes = new ArrayList<>();
        
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return classes;
        }

        try {
            String basePath = dir.getAbsolutePath() + File.separator;
            Files.walk(dir.toPath())
                .filter(Files::isRegularFile)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .filter(this::isPublicClass)
                .map(path -> toClassNameFromPath(path, basePath))
                .forEach(classes::add);
        } catch (IOException e) {
            // Silently ignore errors
        }

        return classes;
    }

    private String toClassNameFromPath(String path, String basePath) {
        String relativePath = path.substring(basePath.length());
        return relativePath.replace(File.separatorChar, '.').replace(".class", "");
    }

    private boolean isPublicClass(String name) {
        return name.endsWith(".class") && !name.contains("$");
    }

    private String toClassName(String name) {
        return name.substring(1).replace('/', '.').replace(".class", "");
    }
}
