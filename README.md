# Metagen

## Description

The goal of this project is to provide a flexible code generator for graph structures.
Metagen interprets the provided graph (see sample in `specs/sql.xml`) and generates code in a specified language (currently targeting C++ and Java).
Graph can be interpreted in multiple ways (as different design patterns), at the moment we plan to support:
- Generic code structures
- Builder pattern
- State machine pattern
- ...and more to come

## But why?

We wanted to create a decent C++ API for building complex SQL queries in a more convenient way than concatenating strings, but we were too lazy to write it manually. Let's generate it instead!

## Usage

The project is still work in progress, at the moment we are focusing on supporting the Builder pattern for both C++ and Java.
First you can see what is being generated based on `specs/sql.xml` in `sample/java/MetagenProto`.
The generator code can be found in `src/metagen`.
Simply run `mvn exec:java` in `src/metagen` to see the supported options.
