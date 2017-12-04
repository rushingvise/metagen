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
#include "QueryBuilderImpl.h"

using namespace sql;

static void append(std::ostringstream &output, const std::vector<std::string> &items, const char seperator = ',') {
    for (size_t i = 0; i < items.size(); ++i) {
        if (i != 0) {
            output << seperator;
        }
        output << items[i];
    }
}

void QueryBuilderImpl::Logic::selectTransitionSelect(QueryBuilderImpl::Content content, std::vector<std::string> columns) {
    (*content.query) << "SELECT ";
    append(*content.query, columns);
    (*content.query) << " ";
}

void QueryBuilderImpl::Logic::fromTransitionFrom(QueryBuilderImpl::Content content, std::string table) {
    (*content.query) << "FROM " << table << " ";
}

void QueryBuilderImpl::Logic::whereTransitionWhere(QueryBuilderImpl::Content content, Types::Expression expression) {
    (*content.query) << "WHERE (" << (std::string) expression << ") ";
}

void QueryBuilderImpl::Logic::groupByTransitionGroupBy(QueryBuilderImpl::Content content, std::string column) {
    (*content.query) << "GROUP BY " << column << " ";
}

void QueryBuilderImpl::Logic::orderByTransitionOrderBy(QueryBuilderImpl::Content content, std::vector<std::string> columns) {
    (*content.query) << "ORDER BY ";
    append(*content.query, columns);
    (*content.query) << " ";
}

void QueryBuilderImpl::Logic::orderDirectionTransitionAsc(QueryBuilderImpl::Content content) {
    (*content.query) << "ASC ";
}

void QueryBuilderImpl::Logic::orderDirectionTransitionDesc(QueryBuilderImpl::Content content) {
    (*content.query) << "DESC ";
}

Types::Query QueryBuilderImpl::Logic::buildQueryTransformationBuild(QueryBuilderImpl::Content content) {
    auto ret = content.query->str();
    if (!ret.empty()) {
        ret.pop_back();
    }
    return ret;
}
