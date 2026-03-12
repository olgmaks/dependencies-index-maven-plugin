package com.depindex.plugin.service;

import com.depindex.plugin.model.DependencyInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.java.decompiler.api.Decompiler;
import org.jetbrains.java.decompiler.main.decompiler.DirectoryResultSaver;
import org.jetbrains.java.decompiler.main.decompiler.SingleFileSaver;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassInfoService {

    private final String indexDirectory;
    private final String indexFileName;
    private final Gson gson;

    public ClassInfoService(String indexDirectory, String indexFileName) {
        this.indexDirectory = indexDirectory;
        this.indexFileName = indexFileName;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Map<String, Object> getClassInfo(String className) throws IOException {
        File indexFile = new File(indexDirectory, indexFileName);
        
        if (!indexFile.exists()) {
            throw new IllegalStateException(
                "Index not found. Please run 'mvn com.depindex:depindex-maven-plugin:index' first to create the index."
            );
        }

        List<DependencyInfo> dependencies = loadDependencies(indexFile);
        
        for (DependencyInfo dep : dependencies) {
            for (String depClass : dep.getClasses()) {
                if (depClass.equals(className) || depClass.endsWith("." + className)) {
                    return buildClassInfo(dep, depClass);
                }
            }
        }

        for (DependencyInfo dep : dependencies) {
            for (String depClass : dep.getClasses()) {
                if (depClass.toLowerCase().contains(className.toLowerCase())) {
                    return buildClassInfo(dep, depClass);
                }
            }
        }

        throw new IllegalArgumentException("Class '" + className + "' not found in index.");
    }

    public String getClassSource(String className) throws IOException {
        File indexFile = new File(indexDirectory, indexFileName);
        
        if (!indexFile.exists()) {
            throw new IllegalStateException(
                "Index not found. Please run 'mvn com.depindex:depindex-maven-plugin:index' first to create the index."
            );
        }

        List<DependencyInfo> dependencies = loadDependencies(indexFile);
        
        for (DependencyInfo dep : dependencies) {
            for (String depClass : dep.getClasses()) {
                if (depClass.equals(className)) {
                    return getSourceCode(dep, depClass);
                }
            }
        }

        for (DependencyInfo dep : dependencies) {
            for (String depClass : dep.getClasses()) {
                if (depClass.endsWith("." + className)) {
                    return getSourceCode(dep, depClass);
                }
            }
        }

        for (DependencyInfo dep : dependencies) {
            for (String depClass : dep.getClasses()) {
                if (depClass.toLowerCase().contains(className.toLowerCase())) {
                    return getSourceCode(dep, depClass);
                }
            }
        }

        throw new IllegalArgumentException("Class '" + className + "' not found in index.");
    }

    public String saveClassSource(String className, String source) throws IOException {
        File classesDir = new File(indexDirectory, "classes");
        Files.createDirectories(classesDir.toPath());
        
        String fileName = className.replace('.', File.separatorChar) + ".java";
        File outputFile = new File(classesDir, fileName);
        Files.createDirectories(outputFile.getParentFile().toPath());
        
        Files.writeString(outputFile.toPath(), source);
        return outputFile.getAbsolutePath();
    }

    private Map<String, Object> buildClassInfo(DependencyInfo dep, String fullClassName) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("class", fullClassName);
        result.put("groupId", dep.getGroupId());
        result.put("artifactId", dep.getArtifactId());
        result.put("version", dep.getVersion());
        result.put("type", dep.getType());
        result.put("scope", dep.getScope());

        String source = getSourceCode(dep, fullClassName);
        result.put("source", source);

        return result;
    }

    private String getSourceCode(DependencyInfo dep, String className) {
        String version = dep.getVersion();
        if (version == null || version.equals("unknown") || version.startsWith("${")) {
            return "// Version not resolved, cannot retrieve source";
        }

        if ("jdk".equals(dep.getArtifactId()) && "java".equals(dep.getGroupId())) {
            return "// JDK class - source not available. Use IDE or check JDK sources.";
        }

        String sourcesJarPath = dep.getJarPath();
        if (sourcesJarPath != null) {
            String sourcesJar = sourcesJarPath.replace(".jar", "-sources.jar");
            File sourcesJarFile = new File(sourcesJar);
            if (sourcesJarFile.exists()) {
                String sourceFromJar = getSourceFromJar(sourcesJarFile, className);
                if (sourceFromJar != null) {
                    return "/* Source from sources JAR */\n" + sourceFromJar;
                }
            }
        }

        if (dep.getJarPath() != null) {
            File jarFile = new File(dep.getJarPath());
            if (jarFile.exists()) {
                return decompileClass(jarFile, className);
            }
        }

        return "// JAR not found. Cannot retrieve source for " + 
               dep.getGroupId() + ":" + dep.getArtifactId() + ":" + version;
    }

    private String decompileClass(File jarFile, String className) {
        try {
            File tempDir = Files.createTempDirectory("depindex-decompile").toFile();
            
            try {
                File outputDir = new File(tempDir, "output");
                outputDir.mkdirs();
                
                IResultSaver resultSaver = new DirectoryResultSaver(outputDir);
                
                StringBuilder logMessages = new StringBuilder();
                Decompiler decompiler = new Decompiler.Builder()
                    .inputs(jarFile)
                    .output(resultSaver)
                    .option("dgs", "true")
                    .option("rbr", "false")
                    .logger(new IFernflowerLogger() {
                        @Override
                        public void writeMessage(String message, IFernflowerLogger.Severity severity) {
                            logMessages.append("[").append(severity).append("] ").append(message).append("\n");
                        }
                        
                        @Override
                        public void writeMessage(String message, IFernflowerLogger.Severity severity, Throwable throwable) {
                            logMessages.append("[").append(severity).append("] ").append(message).append(" - ").append(throwable).append("\n");
                        }
                    })
                    .build();

                decompiler.decompile();
                
                File decompiledFile = new File(outputDir, className.replace('.', '/') + ".java");
                if (decompiledFile.exists()) {
                    String source = Files.readString(decompiledFile.toPath());
                    return "/* Decompiled with Vineflower */\n" + source;
                }
                
                if (!logMessages.isEmpty()) {
                    return "// Decompilation logs:\n" + logMessages.toString();
                }
                
                return "// Decompilation failed for " + className;
                
            } finally {
                deleteDir(tempDir);
            }
            
        } catch (Exception e) {
            return "// Decompilation error: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    private String getSourceFromJar(File sourcesJar, String className) {
        String classFileName = className.replace('.', '/') + ".java";
        
        try (FileSystem fs = FileSystems.newFileSystem(sourcesJar.toPath(), ClassInfoService.class.getClassLoader())) {
            Path path = fs.getPath("/" + classFileName);
            if (Files.exists(path)) {
                return Files.readString(path);
            }
        } catch (IOException e) {
            return "// Error reading sources: " + e.getMessage();
        }
        return null;
    }

    private List<DependencyInfo> loadDependencies(File indexFile) throws IOException {
        Type listType = new TypeToken<List<DependencyInfo>>(){}.getType();
        List<DependencyInfo> dependencies = gson.fromJson(Files.newBufferedReader(indexFile.toPath()), listType);
        return dependencies != null ? dependencies : new ArrayList<>();
    }

    public boolean indexExists() {
        File indexFile = new File(indexDirectory, indexFileName);
        return indexFile.exists();
    }
}
