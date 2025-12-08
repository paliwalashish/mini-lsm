# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Mini-LSM is a Java implementation of a Log-Structured Merge Tree (LSM) key-value storage engine, based on the [Mini-LSM tutorial](https://skyzh.github.io/mini-lsm/00-preface.html). This is a learning project to understand LSM tree internals.

## Build Commands

```bash
# Build the project
mvn clean compile

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=TestClassName

# Run a single test method
mvn test -Dtest=TestClassName#testMethodName

# Package
mvn package
```

## Technical Requirements

- Java 25 with preview features enabled
- Maven 3.x
- The project uses `--enable-preview` compiler flag for both compilation and test execution

## Architecture

This is a multi-module Maven project:
- **Parent POM** (`pom.xml`): Defines shared configuration and compiler settings
- **mini-lsm-core** (`mini-lsm/`): Core LSM tree implementation

### Core Components

The implementation follows standard LSM tree architecture:
- **MemTable** (`io.sigstkflt.memtable`): In-memory table interface supporting get, put, delete operations with approximate size tracking

### LSM Storage Goals
- Implement LSM storage with get, put, and delete operations
- MemTable serves as the write-ahead in-memory buffer before flushing to disk