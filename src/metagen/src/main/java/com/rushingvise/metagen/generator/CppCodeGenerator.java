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

package com.rushingvise.metagen.generator;

import com.rushingvise.metagen.generator.CodeModel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringJoiner;

/**
 * Creates C++ classes based on the provided {@link CodeModel}.
 */
public class CppCodeGenerator extends CodeGenerator {
    private final String mNamespaceName;

    /**
     * Generates C++ code based on the given model.
     * @param outputDirectory Directory in which the C++ files should be created.
     * @param codeModel Model for which the code should be generated.
     * @param namespaceName Namespace that will be defined in the generated files.
     */
    public CppCodeGenerator(String outputDirectory, CodeModel codeModel, String namespaceName) {
        super(outputDirectory, codeModel);
        mNamespaceName = namespaceName;
    }

    @Override
    public void generate() throws CodeGeneratorException {
        try {
            // For each main class model one .cpp and one .h file will be created.
            for (MainClassModel classModel : mCodeModel.classes) {
                File headerFile = new File(mOutputPath, classModel.name + ".h" + (classModel.template ? ".template" : ""));
                if (!headerFile.exists()) {
                    headerFile.createNewFile();
                }
                CodePrintWriter headerWriter = new CodePrintWriter(new FileOutputStream(headerFile));
                File cppFile = new File(mOutputPath, classModel.name + ".cpp" + (classModel.template ? ".template" : ""));
                if (!cppFile.exists()) {
                    cppFile.createNewFile();
                }
                CodePrintWriter cppWriter = new CodePrintWriter(new FileOutputStream(cppFile));
                generateMainClass(classModel, headerWriter, cppWriter);
            }
        } catch (IOException e) {
            throw new CodeGeneratorException(e);
        }
    }

    private void generateClassBody(CppInstructionModelSerializer instructionModelSerializer, AbstractClassModel classModel, CodePrintWriter headerWriter, CodePrintWriter cppWriter) throws CodeGeneratorException {
        // Helper class that will prevent duplication of visibility labels.
        Scope classScope = new Scope();

        // Declaring fields defined in this class.
        for (FieldModel fieldModel : classModel.fieldModels) {
            classScope.updateCurrentVisibility(fieldModel.visibility, headerWriter);
            headerWriter.println(createCppFieldDeclaration(fieldModel) + ";");
            // Static fields require definition in .cpp file.
            if (fieldModel._static) {
                cppWriter.println(createCppFieldDefinition(classModel, fieldModel) + ";");
            }
        }
        headerWriter.println();

        // Declaring constructors of this class.
        for (ConstructorModel constructorModel : classModel.constructorModels) {
            classScope.updateCurrentVisibility(constructorModel.visibility, headerWriter);
            headerWriter.println(createCppConstructorDeclaration(constructorModel) + ";");
            cppWriter.block(createCppConstructorDefinition(constructorModel, instructionModelSerializer), () -> {
                for (InstructionModel instructionModel : constructorModel.constructorBody) {
                    // Unfortunately super call requires special handling.
                    if (!(instructionModel instanceof SuperCallModel)) {
                        cppWriter.println(instructionModel.accept(instructionModelSerializer) + ';');
                    }
                }
            });
            cppWriter.println();
        }
        // Declaring methods of this class.
        for (MethodModel methodModel : classModel.methodModels) {
            classScope.updateCurrentVisibility(methodModel.visibility, headerWriter);
            headerWriter.println(createCppClassMethodDeclaration(methodModel) + ";");
            cppWriter.block(createCppClassMethodDefinition(classModel, methodModel), () -> {
                for (InstructionModel instructionModel : methodModel.methodBody) {
                    cppWriter.println(instructionModel.accept(instructionModelSerializer) + ';');
                }
            });
            cppWriter.println();
        }
    }

    private void generateMainClass(MainClassModel mainClassModel, CodePrintWriter headerWriter, CodePrintWriter cppWriter) throws CodeGeneratorException {
        final CppInstructionModelSerializer instructionModelSerializer = new CppInstructionModelSerializer();
        headerWriter.println("// GENERATED BY METAGEN");
        headerWriter.println("#pragma once");
        headerWriter.println();
        headerWriter.println("#include <memory>");
        headerWriter.println("#include <string>");
        headerWriter.println("#include <vector>");
        for (MainClassModel requiredClass : mainClassModel.requiredClasses) {
            headerWriter.println("#include \"" + requiredClass.name + ".h\"");
        }
        headerWriter.println();

        cppWriter.println("// GENERATED BY METAGEN");
        cppWriter.println("#include \"" + mainClassModel.name + ".h\"");
        cppWriter.println();

        if (mNamespaceName != null) {
            headerWriter.println("namespace " + mNamespaceName + " {");
            headerWriter.println();

            cppWriter.println("using namespace " + mNamespaceName + ";");
            cppWriter.println();
        }

        headerWriter.openBlock(createCppClass(mainClassModel));

        // Helper class that will prevent duplication of visibility labels.
        Scope mainClassScope = new Scope();

        // Forward declaration
        for (InterfaceModel interfaceModel : mainClassModel.interfaces) {
            mainClassScope.updateCurrentVisibility(mainClassModel.visibility, headerWriter);
            headerWriter.println("class " + interfaceModel.name + ";");
        }

        // Forward declaration
        for (InnerClassModel innerClass : mainClassModel.innerClasses) {
            mainClassScope.updateCurrentVisibility(mainClassModel.visibility, headerWriter);
            headerWriter.println("class " + innerClass.name + ";");
        }

        generateClassBody(instructionModelSerializer, mainClassModel, headerWriter, cppWriter);

        // Declaring interfaces defined in this class model.
        for (InterfaceModel interfaceModel : mainClassModel.interfaces) {
            mainClassScope.updateCurrentVisibility(interfaceModel.visibility, headerWriter);
            headerWriter.block(createCppInterface(interfaceModel), () -> {
                for (MethodModel methodModel : interfaceModel.methodModels) {
                    headerWriter.println(createCppInterfaceMethodDeclaration(methodModel) + ";");
                }
            }, ";");
            headerWriter.println();
        }

        // Declaring classes defined in this class model.
        for (InnerClassModel innerClass : mainClassModel.innerClasses) {
            mainClassScope.updateCurrentVisibility(innerClass.visibility, headerWriter);
            headerWriter.openBlock(createCppClass(innerClass));
            generateClassBody(instructionModelSerializer, innerClass, headerWriter, cppWriter);
            headerWriter.closeBlock(";");
        }

        headerWriter.closeBlock(";");

        if (mNamespaceName != null) {
            headerWriter.println("}");
            headerWriter.println();
        }
    }

    private String createCppInterface(InterfaceModel interfaceModel) {
        return "class " + interfaceModel.name;
    }

    private String createCppClass(AbstractClassModel classModel) {
        StringBuilder ret = new StringBuilder();
        ret.append("class ");
        ret.append(classModel.name);
        ret.append(' ');
        if (classModel.superClass != null) {
            ret.append(": public ");
            ret.append(createCppType(new TypeModel(classModel.superClass)));
        }
        if (classModel.implementedInterfaceModels.size() > 0) {
            StringJoiner interfaces = new StringJoiner(", public ", classModel.superClass == null ? ": public " : "", "");
            for (InterfaceModel interfaceModel : classModel.implementedInterfaceModels) {
                interfaces.add(interfaceModel.name);
            }
            ret.append(interfaces.toString());
        }
        return ret.toString();
    }

    private static String createCppVisibility(Visibility visibility) {
        switch (visibility) {
            case PUBLIC:
            default:
                return "public:";
            case PRIVATE:
                return "private:";
            case PROTECTED:
                return "protected:";
        }
    }

    private static String createCppInterfaceMethodDeclaration(MethodModel model) {
        StringBuilder ret = new StringBuilder();
        ret.append("virtual ");
        ret.append(createCppType(model.returnType));
        ret.append(' ');
        ret.append(model.name);
        StringJoiner arguments = new StringJoiner(", ", "(", ")");
        for (ArgumentModel argumentModel : model.argumentModels) {
            arguments.add(createCppArgument(argumentModel));
        }
        ret.append(arguments.toString());
        ret.append(" =0");
        return ret.toString();
    }

    private static String createCppClassMethodDeclaration(MethodModel model) {
        StringBuilder ret = new StringBuilder();
        if (model._static) {
            ret.append("static ");
        }
        ret.append(createCppType(model.returnType));
        ret.append(' ');
        ret.append(model.name);
        StringJoiner arguments = new StringJoiner(", ", "(", ")");
        for (ArgumentModel argumentModel : model.argumentModels) {
            arguments.add(createCppArgument(argumentModel));
        }
        ret.append(arguments.toString());
        if (model.overrides) {
            ret.append(" override");
        }
        return ret.toString();
    }

    private static String createCppClassMethodDefinition(AbstractClassModel classModel, MethodModel model) {
        StringBuilder ret = new StringBuilder();
        ret.append(createCppType(model.returnType));
        ret.append(' ');
        ret.append(createCppClassPath(classModel));
        ret.append("::");
        ret.append(model.name);
        StringJoiner arguments = new StringJoiner(", ", "(", ")");
        for (ArgumentModel argumentModel : model.argumentModels) {
            arguments.add(createCppArgument(argumentModel));
        }
        ret.append(arguments.toString());
        return ret.toString();
    }

    private static String createCppConstructorDeclaration(ConstructorModel model) {
        StringBuilder ret = new StringBuilder();
        ret.append(model.classModel.name);
        StringJoiner arguments = new StringJoiner(", ", "(", ")");
        for (ArgumentModel argumentModel : model.argumentModels) {
            arguments.add(createCppArgument(argumentModel));
        }
        ret.append(arguments.toString());
        return ret.toString();
    }

    private static String createCppConstructorDefinition(ConstructorModel model, CppInstructionModelSerializer serializer) {
        StringBuilder ret = new StringBuilder();
        ret.append(createCppClassPath(model.classModel));
        ret.append("::");
        ret.append(model.classModel.name);
        StringJoiner arguments = new StringJoiner(", ", "(", ")");
        for (ArgumentModel argumentModel : model.argumentModels) {
            arguments.add(createCppArgument(argumentModel));
        }
        ret.append(arguments.toString());

        // Special handling of the super call.
        if (model.constructorBody.size() > 0) {
            InstructionModel instructionModel = model.constructorBody.get(0);
            if (instructionModel instanceof SuperCallModel) {
                SuperCallModel superCallModel = (SuperCallModel) instructionModel;
                ret.append(" : ").append(serializer.visit(superCallModel));
            }
        }
        return ret.toString();
    }

    private static String createCppArgument(ArgumentModel argumentModel) {
        StringBuilder ret = new StringBuilder();
        if (argumentModel.variadic || argumentModel.array) {
            ret.append("std::vector<").append(createCppType(argumentModel.type)).append(">");
        } else {
            ret.append(createCppType(argumentModel.type));
        }
        ret.append(' ');
        ret.append(argumentModel.name);
        return ret.toString();
    }

    private static String createCppFieldDeclaration(FieldModel fieldModel) {
        StringBuilder ret = new StringBuilder();
        if (fieldModel._static) {
            ret.append("static ");
        }
        ret.append(' ');
        ret.append(createCppType(fieldModel.type));
        ret.append(' ');
        ret.append(fieldModel.name);
        return ret.toString();
    }

    private static String createCppFieldDefinition(AbstractClassModel classModel, FieldModel fieldModel) {
        StringBuilder ret = new StringBuilder();
        ret.append(createCppType(fieldModel.type));
        ret.append(' ');
        ret.append(createCppClassPath(classModel));
        ret.append("::");
        ret.append(fieldModel.name);
        return ret.toString();
    }

    private static String createCppType(TypeModel type) {
        if (type == TypeModel.TYPE_VOID) {
            return "void";
        } else if (type == TypeModel.TYPE_STRING) {
            return "std::string";
        } else if (type == TypeModel.TYPE_INTEGER) {
            return "int";
        } else {
            String ret;
            if (type.name != null) {
                ret = type.name;
            } else {
                EntityModel entityModel = type.entityModel;
                ret = createCppClassPath(entityModel);
            }
            if (type.reference) {
                return "std::shared_ptr<" + ret + ">";
            } else {
                return ret;
            }
        }
    }

    private static String createCppClassPath(EntityModel entityModel) {
        if (entityModel instanceof InnerEntityModel) {
            return ((InnerEntityModel) entityModel).getOuterClass().name + "::" + entityModel.name;
        } else {
            return entityModel.name;
        }
    }

    private static class CppInstructionModelSerializer implements InstructionModelSerializer {
        @Override
        public String visit(StringValueModel stringValueModel) {
            return "\"" + stringValueModel.toString() + "\"";
        }

        @Override
        public String visit(IntegerValueModel integerValueModel) {
            return integerValueModel.value.toString();
        }

        @Override
        public String visit(NullValueModel nullValueModel) {
            return "nullptr";
        }

        @Override
        public String visit(VariableModel variableModel) {
            return variableModel.name;
        }

        @Override
        public String visit(DeclarationModel declarationModel) {
            return declarationModel.variable.type + " " + declarationModel.variable.name;
        }

        @Override
        public String visit(AssignmentModel assignmentModel) {
            return assignmentModel.leftValue.accept(this) + " = " + assignmentModel.rightValue.accept(this);
        }

        @Override
        public String visit(ReturnInstructionModel returnInstructionModel) {
            return "return " + ((returnInstructionModel.returnedStatement != null) ? returnInstructionModel.returnedStatement.accept(this) : "nullptr");
        }

        @Override
        public String visit(MethodCallModel methodCallModel) {
            StringBuilder ret = new StringBuilder();
            if (methodCallModel.classInstance != null) {
                ret.append(createCppClassPath(methodCallModel.classInstance)).append("::");
            } else if (methodCallModel.instance != null) {
                ret.append(methodCallModel.instance.name);
                if (methodCallModel.instance.type.reference) {
                    ret.append("->");
                } else {
                    ret.append('.');
                }
            }
            ret.append(methodCallModel.methodName);
            StringJoiner arguments = new StringJoiner(", ", "(", ")");
            for (RValueModel rValueModel : methodCallModel.parameters) {
                arguments.add(rValueModel.accept(this));
            }
            ret.append(arguments.toString());
            return ret.toString();
        }

        @Override
        public String visit(AllocationModel allocationModel) {
            StringBuilder ret = new StringBuilder();
            if (allocationModel.type.reference) {
                ret.append("std::make_shared<");
                ret.append(createCppType(allocationModel.type));
                ret.append(">");
            } else {
                ret.append(createCppType(allocationModel.type));
            }
            StringJoiner arguments = new StringJoiner(", ", "(", ")");
            for (RValueModel rValueModel : allocationModel.parameters) {
                arguments.add(rValueModel.accept(this));
            }
            ret.append(arguments.toString());
            return ret.toString();
        }

        @Override
        public String visit(SuperCallModel superCallModel) {
            StringBuilder ret = new StringBuilder();
            ret.append(createCppType(new TypeModel(superCallModel.superClass)));
            StringJoiner arguments = new StringJoiner(", ", "(", ")");
            for (RValueModel rValueModel : superCallModel.parameters) {
                arguments.add(rValueModel.accept(this));
            }
            ret.append(arguments.toString());
            return ret.toString();
        }
    }

    /**
     * Helper class that will prevent duplication of visibility labels.
     */
    private static class Scope {
        private Visibility mVisibility;

        private void updateCurrentVisibility(Visibility visibility, CodePrintWriter writer) {
            if (mVisibility != visibility) {
                mVisibility = visibility;
                if (mVisibility != null) {
                    writer.println(createCppVisibility(mVisibility));
                }
            }
        }
    }
}

