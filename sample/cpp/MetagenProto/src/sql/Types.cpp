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

#include "Types.h"

using namespace sql;

Types::Expression::Expression(const std::string& value) {
    mValue = value;
}

Types::Expression::operator std::string() {
    return mValue;
}

Types::Query::Query(const std::string& value) {
    mValue = value;
}

Types::Query::operator std::string() {
    return mValue;
}
