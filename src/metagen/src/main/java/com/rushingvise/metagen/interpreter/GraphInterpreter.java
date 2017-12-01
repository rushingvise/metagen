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

package com.rushingvise.metagen.interpreter;

import com.rushingvise.metagen.generator.CodeModel;
import com.rushingvise.metagen.parser.GraphsModel;

/**
 * Abstract class for all graph interpreters.
 */
public abstract class GraphInterpreter {
    protected final GraphsModel mGraphsModel;

    /**
     * @param graphsModel Graphs specification.
     */
    public GraphInterpreter(GraphsModel graphsModel) {
        mGraphsModel = graphsModel;
    }

    /**
     * Main function responsible for analyzing the graphs specification.
     * Should be implemented in the child classes.
     * @return {@code CodeModel} for the given interpretation type.
     * @throws GraphInterpreterException
     */
    public abstract CodeModel analyze() throws GraphInterpreterException;
}
