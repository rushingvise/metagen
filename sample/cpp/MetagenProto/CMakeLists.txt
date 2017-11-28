cmake_minimum_required(VERSION 3.8)
project(MetagenProto)

set(CMAKE_CXX_STANDARD 11)

set(SOURCE_FILES
        src/main.cpp
        src/sql/QueryBuilder.h
        src/sql/QueryBuilderApi.h
        src/sql/QueryBuilderImpl.h
        )
add_executable(MetagenProto ${SOURCE_FILES})