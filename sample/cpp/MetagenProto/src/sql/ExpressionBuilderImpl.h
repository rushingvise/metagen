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
#include "Types.h"

#include <sstream>

namespace sql {

class ExpressionBuilderImpl  {
    public:
    class Content;
    class Logic;

    class Content  {
    public:
        std::shared_ptr<std::ostringstream> expression;

        Content() {
            expression = std::make_shared<std::ostringstream>();
        }
    };
    class Logic  {

        public:
        static void expressionTransitionTableColumn(ExpressionBuilderImpl::Content content, std::string table, std::string column);
        static void expressionTransitionColumn(ExpressionBuilderImpl::Content content, std::string column);
        static void expressionTransitionString(ExpressionBuilderImpl::Content content, std::string value);
        static void expressionTransitionNumber(ExpressionBuilderImpl::Content content, int value);
        static void expressionTransitionExpression(ExpressionBuilderImpl::Content content, Types::Expression expression);
        static void binaryOperationTransitionEquals(ExpressionBuilderImpl::Content content);
        static void binaryOperationTransitionLessThan(ExpressionBuilderImpl::Content content);
        static void binaryOperationTransitionLessThanOrEqual(ExpressionBuilderImpl::Content content);
        static void binaryOperationTransitionGreaterThan(ExpressionBuilderImpl::Content content);
        static void binaryOperationTransitionGreaterThanOrEqual(ExpressionBuilderImpl::Content content);
        static void binaryOperationTransitionAnd(ExpressionBuilderImpl::Content content);
        static void binaryOperationTransitionOr(ExpressionBuilderImpl::Content content);
        static void binaryOperationTransitionPlus(ExpressionBuilderImpl::Content content);
        static void binaryOperationTransitionMinus(ExpressionBuilderImpl::Content content);
        static Types::Expression buildExpressionTransformationBuild(ExpressionBuilderImpl::Content content);
    };
};
}

