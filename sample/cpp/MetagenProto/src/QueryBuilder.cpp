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

#include "QueryBuilder.h"

using namespace sqleicht;

static void append(std::ostringstream &output, const std::vector<std::string> &items, const char seperator = ',') {
    for (size_t i = 0; i < items.size(); ++i) {
        if (i != 0) {
            output << seperator;
        }
        output << items[i];
    }
}

QueryBlock::QueryBlock(QueryState state) : mState(state) {};

QueryBuilder::QueryBuilder() : Select(QueryState(new std::ostringstream)) {
}


Select::Select(QueryState state) : QueryBlock(state) {}

PostSelect Select::select(const std::string &column) {
    (*mState) << "SELECT " << column << " ";
    return PostSelect(mState);
}

PostSelect Select::select(const std::vector<std::string> &columns) {
    (*mState) << "SELECT ";
    append(*mState, columns);
    (*mState) << " ";
    return PostSelect(mState);
}

PostSelect::PostSelect(QueryState state) : From(state) {}

From::From(QueryState state) : QueryBlock(state) {}

PostFrom From::from(const std::string &table) {
    (*mState) << "FROM " << table << " ";
    return PostFrom(mState);
}

PostFrom::PostFrom(QueryState state) : QueryEnd(state), Where(state), GroupBy(state), OrderBy(state) {}

QueryEnd::QueryEnd(QueryState state) : QueryBlock(state) {}

std::string QueryEnd::build() {
    return mState->str();
}

Where::Where(QueryState state) : QueryBlock(state) {}

PostWhere Where::where(const Expression &expression) {
    (*mState) << "WHERE (" << expression << ") ";
    return PostWhere(mState);
}

PostWhere::PostWhere(QueryState state) : GroupBy(state), OrderBy(state), QueryEnd(state) {}

GroupBy::GroupBy(QueryState state) : QueryBlock(state) {}

PostGroupBy GroupBy::groupBy(const std::string &column) {
    (*mState) << "GROUP BY " << column << " ";
    return PostGroupBy(mState);
}

PostGroupBy::PostGroupBy(QueryState state) : OrderBy(state), QueryEnd(state) {}

OrderBy::OrderBy(QueryState state) : QueryBlock(state) {}

PostOrderBy OrderBy::orderBy(const std::string &column) {
    (*mState) << "ORDER BY " << column << " ";
    return PostOrderBy(mState);
}

PostOrderBy OrderBy::orderBy(const std::vector<std::string>& columns) {
    (*mState) << "ORDER BY ";
    append(*mState, columns);
    (*mState) << " ";
    return PostOrderBy(mState);
}

PostOrderBy::PostOrderBy(QueryState state) : OrderDirection(state) {}

OrderDirection::OrderDirection(QueryState state) : QueryBlock(state) {}

PostOrderDirection OrderDirection::asc() {
    (*mState) << "ASC ";
    return PostOrderDirection(mState);
}

PostOrderDirection OrderDirection::desc() {
    (*mState) << "DESC ";
    return PostOrderDirection(mState);
}

PostOrderDirection::PostOrderDirection(QueryState state) : QueryEnd(state) {}
