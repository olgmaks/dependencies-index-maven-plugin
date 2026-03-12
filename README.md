# Dependency Index Maven Plugin

A Maven plugin to index and search project dependencies and their classes.

## Features

- Index all project dependencies (Maven, JDK, and current project classes)
- Search for classes across all indexed dependencies
- View class source code (from sources JAR or decompiled)
- Fast local JSON-based index

## Usage

### Search for a class

```bash
mvn com.depindex:depindex-maven-plugin:search -Ddepindex.q=ArrayList
```

### Search with reindex

Rebuild the index before searching:

```bash
mvn com.depindex:depindex-maven-plugin:search -Ddepindex.q=ArrayList -Ddepindex.reindex=true
```

### Get class information with source code

```bash
mvn com.depindex:depindex-maven-plugin:classinfo -Ddepindex.class=java.util.ArrayList
```

## Properties

| Property | Description | Default |
|----------|-------------|---------|
| `depindex.q` | Class name to search for (required for search) | - |
| `depindex.class` | Full class name to get info for (required for classinfo) | - |
| `depindex.outputDirectory` | Directory to store the index | `.depindex` |
| `depindex.outputFile` | Index file name | `dependencies.json` |
| `depindex.maxClasses` | Maximum number of classes to index per dependency | `10000` |
| `depindex.search.limit` | Maximum number of search results | `10` |
| `depindex.reindex` | Force rebuild of index before search | `false` |
| `depindex.out` | Output file path for classinfo command | stdout |

## Examples

### Search with custom result limit

```bash
mvn depindex:search -Ddepindex.q=List -Ddepindex.search.limit=20
```

### Search with custom output directory

```bash
mvn depindex:search -Ddepindex.q=Map -Ddepindex.outputDirectory=.depindex
```

### Get class info and save to file

```bash
mvn depindex:classinfo -Ddepindex.class=com.google.common.collect.Lists -Ddepindex.out=/tmp/Lists.java
```

## Index Contents

The index includes:

1. **Current project classes** - compiled classes from `target/classes`
2. **Maven dependencies** - all transitive dependencies
3. **JDK classes** - Java standard library classes

## Index Contents

### Build

```bash
mvn clean install
```

### Run tests

```bash
mvn test
```
