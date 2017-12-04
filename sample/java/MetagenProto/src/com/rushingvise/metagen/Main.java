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

package com.rushingvise.metagen;

import com.rushingvise.metagen.sql.ExpressionBuilder;
import com.rushingvise.metagen.sql.QueryBuilder;
import com.rushingvise.metagen.sql.Types;

public class Main {

    public static void main(String[] args) {
        new ExpressionBuilder().column("id").greaterThan().number(3).build();
        Types.Query query = new QueryBuilder().select("id", "user", "pass").from("users").where(new ExpressionBuilder().column("id").greaterThan().number(3).build()).orderBy("id").asc().build();
        System.out.println(query);
    }
}
