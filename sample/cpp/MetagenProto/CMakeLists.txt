cmake_minimum_required(VERSION 3.8)
project(MetagenProto)

set(CMAKE_CXX_STANDARD 11)

set(SOURCE_FILES
        src/main.cpp
        src/sql/QueryBuilder.cpp
        src/sql/QueryBuilderApi.cpp
        src/sql/QueryBuilderImpl.cpp
        )
add_executable(MetagenProto ${SOURCE_FILES})