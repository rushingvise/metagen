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

package com.rushingvise.metagen.utils;

public abstract class Utils {
    public static void appendItems(StringBuilder output, String[] items, char separator) {
        for (int i = 0; i < items.length; ++i) {
            if (i != 0) {
                output.append(separator);
            }
            output.append(items[i]);
        }
    }
}
