package com.depindex.plugin.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DependencyInfo {

    private String groupId;
    private String artifactId;
    private String version;
    private String type;
    private String scope;
    private List<String> classes = new ArrayList<>();
    private String jarPath;

    public DependencyInfo() {
    }

    public DependencyInfo(String groupId, String artifactId, String version, String type, String scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.scope = scope;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("groupId", groupId);
        map.put("artifactId", artifactId);
        map.put("version", version);
        map.put("type", type);
        map.put("scope", scope);
        map.put("classes", classes);
        if (jarPath != null) {
            map.put("jarPath", jarPath);
        }
        return map;
    }
}
