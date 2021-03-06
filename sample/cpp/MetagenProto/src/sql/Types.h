// GENERATED BY METAGEN
#pragma once

#include <memory>
#include <string>
#include <vector>

namespace sql {

class Types  {
    public:
    class Expression;
    class Query;

    class Expression  {
    public:
        Expression(const std::string& value);
        operator std::string();
    private:
        std::string mValue;
    };
    class Query  {
    public:
        Query(const std::string& value);
        operator std::string();
    private:
        std::string mValue;
    };
};
}

