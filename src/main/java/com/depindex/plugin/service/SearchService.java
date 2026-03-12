package com.depindex.plugin.service;

import com.depindex.plugin.model.DependencyInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchService {

    private static final int DEFAULT_LIMIT = 10;

    private final String indexDirectory;
    private final String indexFileName;
    private final Gson gson;

    public SearchService(String indexDirectory, String indexFileName) {
        this.indexDirectory = indexDirectory;
        this.indexFileName = indexFileName;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public List<Map<String, Object>> search(String className) throws IOException {
        return search(className, DEFAULT_LIMIT);
    }

    public List<Map<String, Object>> search(String className, int limit) throws IOException {
        File indexFile = getIndexFile();
        
        if (!indexFile.exists()) {
            throw new IllegalStateException(
                "Index not found. Please run 'mvn com.depindex:depindex-maven-plugin:index' first to create the index."
            );
        }

        List<DependencyInfo> dependencies = loadDependencies(indexFile);
        String searchPattern = className;

        List<Map<String, Object>> results = new ArrayList<>();
        
        for (DependencyInfo dep : dependencies) {
            for (String matchedClass : dep.getClasses()) {
                if (matchedClass.contains(searchPattern)) {
                    int priority = calculatePriority(matchedClass, searchPattern);
                    if (priority > 0) {
                        Map<String, Object> result = toSearchResult(dep, matchedClass);
                        result.put("priority", priority);
                        results.add(result);
                    }
                }
            }
        }

        results.sort(Comparator.<Map<String, Object>>comparingInt(m -> (Integer) m.get("priority"))
            .reversed()
            .thenComparing(m -> ((String) m.get("class")).length())
            .thenComparing(m -> (String) m.get("class")));

        if (limit > 0 && results.size() > limit) {
            results = results.subList(0, limit);
        }

        results.forEach(m -> m.remove("priority"));
        
        return results;
    }

    private int calculatePriority(String className, String searchPattern) {
        if (className.equals(searchPattern)) {
            return 100;
        }
        
        String shortName = className.substring(className.lastIndexOf('.') + 1);
        if (shortName.equals(searchPattern)) {
            return 90;
        }
        
        if (className.startsWith(searchPattern)) {
            return 80;
        }
        
        if (shortName.startsWith(searchPattern)) {
            return 70;
        }
        
        return 60;
    }

    private List<DependencyInfo> loadDependencies(File indexFile) throws IOException {
        try (FileReader reader = new FileReader(indexFile)) {
            Type listType = new TypeToken<List<DependencyInfo>>(){}.getType();
            List<DependencyInfo> dependencies = gson.fromJson(reader, listType);
            return dependencies != null ? dependencies : new ArrayList<>();
        }
    }

    private Map<String, Object> toSearchResult(DependencyInfo dep, String matchedClass) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("class", matchedClass);
        result.put("groupId", dep.getGroupId());
        result.put("artifactId", dep.getArtifactId());
        result.put("version", dep.getVersion());
        result.put("type", dep.getType());
        result.put("scope", dep.getScope());
        return result;
    }

    public File getIndexFile() {
        return new File(indexDirectory, indexFileName);
    }

    public boolean indexExists() {
        return getIndexFile().exists();
    }

    public String getIndexDirectory() {
        return indexDirectory;
    }

    public String getIndexFileName() {
        return indexFileName;
    }
}
