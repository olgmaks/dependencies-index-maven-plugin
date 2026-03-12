package com.depindex.plugin.service;

import com.depindex.plugin.model.DependencyInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class IndexWriterService {

    private final File outputDirectory;
    private final String outputFileName;
    private final Gson gson;

    public IndexWriterService(String outputDirectory, String outputFileName) {
        this.outputDirectory = new File(outputDirectory);
        this.outputFileName = outputFileName;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void write(List<DependencyInfo> dependencies) throws IOException {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        File output = new File(outputDirectory, outputFileName);
        
        try (FileWriter writer = new FileWriter(output)) {
            List<?> jsonList = dependencies.stream()
                .map(DependencyInfo::toMap)
                .toList();
            gson.toJson(jsonList, writer);
        }
    }

    public File getOutputFile() {
        return new File(outputDirectory, outputFileName);
    }
}
