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

public class ExpressionBuilderImpl  {

    public static class Content  {
        StringBuilder expression = new StringBuilder();
    }
    public static class Logic  {

        public static void expressionTransitionTableColumn(ExpressionBuilderImpl.Content content, String table, String column) {
            content.expression.append(table).append('.').append(column).append(' ');
        }

        public static void expressionTransitionColumn(ExpressionBuilderImpl.Content content, String column) {
            content.expression.append(column).append(' ');
        }

        public static void expressionTransitionString(ExpressionBuilderImpl.Content content, String value) {
            content.expression.append('"').append(value).append("\" ");
        }

        public static void expressionTransitionNumber(ExpressionBuilderImpl.Content content, int value) {
            content.expression.append(value).append(' ');
        }

        public static void expressionTransitionExpression(ExpressionBuilderImpl.Content content, Types.Expression expression) {
            content.expression.append(expression.toString()).append(' ');
        }

        public static void binaryOperationTransitionEquals(ExpressionBuilderImpl.Content content) {
            content.expression.append("= ");
        }

        public static void binaryOperationTransitionLessThan(ExpressionBuilderImpl.Content content) {
            content.expression.append("< ");
        }

        public static void binaryOperationTransitionLessThanOrEqual(ExpressionBuilderImpl.Content content) {
            content.expression.append("<= ");
        }

        public static void binaryOperationTransitionGreaterThan(ExpressionBuilderImpl.Content content) {
            content.expression.append("> ");
        }

        public static void binaryOperationTransitionGreaterThanOrEqual(ExpressionBuilderImpl.Content content) {
            content.expression.append(">= ");
        }

        public static void binaryOperationTransitionAnd(ExpressionBuilderImpl.Content content) {
            content.expression.append("AND ");
        }

        public static void binaryOperationTransitionOr(ExpressionBuilderImpl.Content content) {
            content.expression.append("OR ");
        }

        public static void binaryOperationTransitionPlus(ExpressionBuilderImpl.Content content) {
            content.expression.append("+ ");
        }

        public static void binaryOperationTransitionMinus(ExpressionBuilderImpl.Content content) {
            content.expression.append("- ");
        }

        public static Types.Expression buildExpressionTransformationBuild(ExpressionBuilderImpl.Content content) {
            return new Types.Expression(content.expression.toString().trim());
        }

    }
}
