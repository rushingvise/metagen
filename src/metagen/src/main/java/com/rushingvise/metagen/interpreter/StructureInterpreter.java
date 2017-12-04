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
import com.rushingvise.metagen.parser.StructureModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for all structure interpreters.
 */
public abstract class StructureInterpreter {
    protected final StructureModel mStructureModel;
    protected CodeModel.MainClassModel mTypesMainClass;
    protected Map<String, CodeModel.InnerClassModel> mTypes = new HashMap<>();

    /**
     * @param structureModel Graphs specification.
     */
    public StructureInterpreter(StructureModel structureModel) {
        mStructureModel = structureModel;
    }

    /**
     * Main function responsible for analyzing the graphs specification.
     * Should be implemented in the child classes.
     * @return {@code CodeModel} for the given interpretation type.
     * @throws StructureInterpreterException
     */
    public CodeModel analyze() throws StructureInterpreterException {
        analyzeTypes();
        return analyzeGraphs();
    }

    protected void analyzeTypes() {
        if (mStructureModel.types != null && mStructureModel.types.size() > 0) {
            mTypesMainClass = new CodeModel.MainClassModel("Types");
            mTypesMainClass.template = true;
            for (StructureModel.TypeModel typeModel : mStructureModel.types) {
                CodeModel.InnerClassModel typeClass = new CodeModel.InnerClassModel(typeModel.name, mTypesMainClass);

                mTypes.put(typeClass.name, typeClass);
            }
        }
    }

    protected abstract CodeModel analyzeGraphs() throws StructureInterpreterException;
}
