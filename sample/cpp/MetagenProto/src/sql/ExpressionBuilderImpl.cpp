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
#include "ExpressionBuilderImpl.h"

using namespace sql;

void ExpressionBuilderImpl::Logic::expressionTransitionTableColumn(ExpressionBuilderImpl::Content content, std::string table, std::string column) {
    (*content.expression) << table<< '.' << column << ' ';
}

void ExpressionBuilderImpl::Logic::expressionTransitionColumn(ExpressionBuilderImpl::Content content, std::string column) {
    (*content.expression) << column << ' ';
}

void ExpressionBuilderImpl::Logic::expressionTransitionString(ExpressionBuilderImpl::Content content, std::string value) {
    (*content.expression) << '"' << value << "\" ";
}

void ExpressionBuilderImpl::Logic::expressionTransitionNumber(ExpressionBuilderImpl::Content content, int value) {
    (*content.expression) << value << ' ';
}

void ExpressionBuilderImpl::Logic::expressionTransitionExpression(ExpressionBuilderImpl::Content content, Types::Expression expression) {
    (*content.expression) << (std::string) expression << ' ';
}

void ExpressionBuilderImpl::Logic::binaryOperationTransitionEquals(ExpressionBuilderImpl::Content content) {
    (*content.expression) << "= ";
}

void ExpressionBuilderImpl::Logic::binaryOperationTransitionLessThan(ExpressionBuilderImpl::Content content) {
    (*content.expression) << "< ";
}

void ExpressionBuilderImpl::Logic::binaryOperationTransitionLessThanOrEqual(ExpressionBuilderImpl::Content content) {
    (*content.expression) << "<= ";
}

void ExpressionBuilderImpl::Logic::binaryOperationTransitionGreaterThan(ExpressionBuilderImpl::Content content) {
    (*content.expression) << "> ";
}

void ExpressionBuilderImpl::Logic::binaryOperationTransitionGreaterThanOrEqual(ExpressionBuilderImpl::Content content) {
    (*content.expression) << ">= ";
}

void ExpressionBuilderImpl::Logic::binaryOperationTransitionAnd(ExpressionBuilderImpl::Content content) {
    (*content.expression) << "AND ";
}

void ExpressionBuilderImpl::Logic::binaryOperationTransitionOr(ExpressionBuilderImpl::Content content) {
    (*content.expression) << "OR ";
}

void ExpressionBuilderImpl::Logic::binaryOperationTransitionPlus(ExpressionBuilderImpl::Content content) {
    (*content.expression) << "+ ";
}

void ExpressionBuilderImpl::Logic::binaryOperationTransitionMinus(ExpressionBuilderImpl::Content content) {
    (*content.expression) << "- ";
}

Types::Expression ExpressionBuilderImpl::Logic::buildExpressionTransformationBuild(ExpressionBuilderImpl::Content content) {
    auto ret = content.expression->str();
    if (!ret.empty()) {
        ret.pop_back();
    }
    return ret;
}

