cmake_minimum_required(VERSION 3.8)
project(MetagenProto)

set(CMAKE_CXX_STANDARD 11)

set(SOURCE_FILES
        src/main.cpp
        src/QueryBuilder.cpp
        )
add_executable(MetagenProto ${SOURCE_FILES})