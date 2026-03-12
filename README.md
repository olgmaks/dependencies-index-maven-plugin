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

Class source is saved to `.depindex/classes/<package>/<ClassName>.java`:

```bash
mvn com.depindex:depindex-maven-plugin:classinfo -Ddepindex.class=java.util.ArrayList
```

## Properties

| Property | Description | Default |
|----------|-------------|---------|
| `depindex.q` | Class name to search for (required for search) | - |
| `depindex.class` | Full class name to get info for (required for classinfo) | - |
| `depindex.limit` | Maximum number of search results | `10` |
| `depindex.reindex` | Force rebuild of index before search | `false` |

## Examples

### Search with custom result limit

```bash
mvn com.depindex:depindex-maven-plugin:search -Ddepindex.q=List -Ddepindex.limit=20
```

### Reindex before search

```bash
mvn com.depindex:depindex-maven-plugin:search -Ddepindex.q=List -Ddepindex.reindex=true
```

## Index Contents

The index includes:

1. **Current project classes** - compiled classes from `target/classes`
2. **Maven dependencies** - all compile/runtime scope dependencies
3. **JDK classes** - Java standard library classes

## Output

- Index: `.depindex/dependencies.json`
- Class sources: `.depindex/classes/<classname>.java`

## Development

### Build

```bash
mvn clean install
```
