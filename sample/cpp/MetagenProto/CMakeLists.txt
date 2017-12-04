cmake_minimum_required(VERSION 3.8)
project(MetagenProto)

set(CMAKE_CXX_STANDARD 11)

set(SOURCE_FILES
        src/main.cpp
        src/sql/Types.cpp
        src/sql/QueryBuilder.cpp
        src/sql/QueryBuilderApi.cpp
        src/sql/QueryBuilderImpl.cpp
        src/sql/ExpressionBuilder.cpp
        src/sql/ExpressionBuilderApi.cpp
        src/sql/ExpressionBuilderImpl.cpp
        )
add_executable(MetagenProto ${SOURCE_FILES})