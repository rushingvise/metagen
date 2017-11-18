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

package com.rushingvise.metagen.sql;

import com.rushingvise.metagen.utils.Utils;

public class QueryBuilderImpl  {

    public static class Content  {
        StringBuilder query = new StringBuilder();
    }

    public static class Logic  {

        public static void selectTransitionSelect(QueryBuilderImpl.Content content, String... columns) {
            content.query.append("SELECT ");
            Utils.appendItems(content.query, columns, ',');
            content.query.append(" ");
        }

        public static void fromTransitionFrom(QueryBuilderImpl.Content content, String table) {
            content.query.append("FROM ").append(table).append(" ");
        }

        public static void whereTransitionWhere(QueryBuilderImpl.Content content, String expression) {
            content.query.append("WHERE (").append(expression).append(") ");
        }

        public static void groupByTransitionGroupBy(QueryBuilderImpl.Content content, String column) {
            content.query.append("GROUP BY ").append(column).append(" ");
        }

        public static void orderByTransitionOrderBy(QueryBuilderImpl.Content content, String... columns) {
            content.query.append("ORDER BY ");
            Utils.appendItems(content.query, columns, ',');
            content.query.append(" ");
        }

        public static void orderDirectionTransitionAsc(QueryBuilderImpl.Content content) {
            content.query.append("ASC ");
        }

        public static void orderDirectionTransitionDesc(QueryBuilderImpl.Content content) {
            content.query.append("DESC ");
        }

        public static String buildQueryTransformationBuild(QueryBuilderImpl.Content content) {
            return content.query.toString();
        }

    }
}
