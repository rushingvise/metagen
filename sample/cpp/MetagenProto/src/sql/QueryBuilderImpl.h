/*
Copyright @ 2017 Rushing Vise OU

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
#pragma once

#include <memory>
#include <string>
#include <vector>
#include <sstream>

namespace sql {

static void append(std::ostringstream &output, const std::vector<std::string> &items, const char seperator = ',') {
    for (size_t i = 0; i < items.size(); ++i) {
        if (i != 0) {
            output << seperator;
        }
        output << items[i];
    }
}

class QueryBuilderImpl  {
    public:
    class Content;
    class Logic;

    class Content  {
    public:
        std::shared_ptr<std::ostringstream> query;

        Content() {
            query = std::make_shared<std::ostringstream>();
        }
    };
    class Logic  {

        public:
        static void selectTransitionSelect(QueryBuilderImpl::Content content, std::vector<std::string> columns) {
            (*content.query) << "SELECT ";
            append(*content.query, columns);
            (*content.query) << " ";
        }

        static void fromTransitionFrom(QueryBuilderImpl::Content content, std::string table) {
            (*content.query) << "FROM " << table << " ";
        }

        static void whereTransitionWhere(QueryBuilderImpl::Content content, std::string expression) {
            (*content.query) << "WHERE (" << expression << ") ";
        }

        static void groupByTransitionGroupBy(QueryBuilderImpl::Content content, std::string column) {
            (*content.query) << "GROUP BY " << column << " ";
        }

        static void orderByTransitionOrderBy(QueryBuilderImpl::Content content, std::vector<std::string> columns) {
            (*content.query) << "ORDER BY ";
            append(*content.query, columns);
            (*content.query) << " ";
        }

        static void orderDirectionTransitionAsc(QueryBuilderImpl::Content content) {
            (*content.query) << "ASC ";
        }

        static void orderDirectionTransitionDesc(QueryBuilderImpl::Content content) {
            (*content.query) << "DESC ";
        }

        static std::string buildQueryTransformationBuild(QueryBuilderImpl::Content content) {
            return content.query->str();
        }

    };
};
}

