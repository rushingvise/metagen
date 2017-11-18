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

#include <string>
#include <vector>
#include <memory>
#include <sstream>

/*
 * SELECT x, y FROM table WHERE x = 3 ORDER BY x ASC | DESC
 */

namespace sqleicht {
    using Expression = std::string;

    using QueryState = std::shared_ptr<std::ostringstream>;

    class QueryBlock {
    protected:
        QueryBlock(QueryState state);
        QueryState mState;
    };

    class QueryEnd : public QueryBlock {
    public:
        QueryEnd(QueryState state);
        std::string build();
    };

    class PostOrderDirection : public QueryEnd {
    public:
        PostOrderDirection(QueryState state);
    };

    class OrderDirection : public QueryBlock {
    public:
        OrderDirection(QueryState state);
        PostOrderDirection asc();
        PostOrderDirection desc();
    };

    class PostOrderBy : public OrderDirection {
    public:
        PostOrderBy(QueryState state);
    };

    class OrderBy : public QueryBlock {
    public:
        OrderBy(QueryState state);
        PostOrderBy orderBy(const std::string &column);
        PostOrderBy orderBy(const std::vector<std::string>& columns);
    };

    class PostGroupBy : public OrderBy, public QueryEnd {
    public:
        PostGroupBy(QueryState state);
    };

    class GroupBy : public QueryBlock {
    public:
        GroupBy(QueryState state);
        PostGroupBy groupBy(const std::string &column);
    };

    class PostWhere : public GroupBy, public OrderBy, public QueryEnd {
    public:
        PostWhere(QueryState state);
    };

    class Where : public QueryBlock {
    public:
        Where(QueryState state);
        PostWhere where(const Expression& expression);
    };

    class PostFrom : public QueryEnd, public Where, public GroupBy, public OrderBy {
    public:
        PostFrom(QueryState state);
    };

    class From : public QueryBlock {
    public:
        From(QueryState state);
        PostFrom from(const std::string &table);
    };

    class PostSelect : public From {
    public:
        PostSelect(QueryState state);
    };

    class Select : public QueryBlock {
    public:
        Select(QueryState state);
        PostSelect select(const std::string &column);
        PostSelect select(const std::vector<std::string> &columns);
    };

    class QueryBuilder : public Select {
    public:
        QueryBuilder();
    };
}
