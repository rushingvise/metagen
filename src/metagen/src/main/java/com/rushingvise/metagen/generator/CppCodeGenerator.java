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

public class CppCodeGenerator extends CodeGenerator {
    private final String mNamespaceName;

    public CppCodeGenerator(String outputDirectory, CodeModel codeModel, String namespaceName) {
        super(outputDirectory, codeModel);
        mNamespaceName = namespaceName;
    }

    @Override
    public void generate() throws CodeGeneratorException {
        try {
            for (ClassModel classModel : mCodeModel.classes) {
                File headerFile = new File(mOutputPath, classModel.name + ".h");
                if (!headerFile.exists()) {
                    headerFile.createNewFile();
                }
                CodePrintWriter headerWriter = new CodePrintWriter(new FileOutputStream(headerFile));
                File cppFile = new File(mOutputPath, classModel.name + ".cpp");
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

    private void generateClass(CppInstructionModelSerializer instructionModelSerializer, ClassModel mainClassModel, ClassModel classModel, CodePrintWriter headerWriter, CodePrintWriter cppWriter) throws CodeGeneratorException {
        if (mainClassModel != classModel) {
            if (classModel.interfaces.size() > 0) {
                throw new CodeGeneratorException("Only main class can contain inner interfaces.");
            }
            if (classModel.classes.size() > 0) {
                throw new CodeGeneratorException("Only main class can contain inner classes.");
            }
        }

        headerWriter.openBlock(createCppClass(classModel));
        Scope classScope = new Scope();

        // Forward declaration
        for (InterfaceModel interfaceModel : classModel.interfaces) {
            classScope.updateCurrentVisibility(classModel.visibility, headerWriter);
            headerWriter.println("class " + interfaceModel.name + ";");
        }

        // Forward declaration
        for (ClassModel innerClass : classModel.classes) {
            classScope.updateCurrentVisibility(classModel.visibility, headerWriter);
            headerWriter.println("class " + innerClass.name + ";");
        }

        for (FieldModel fieldModel : classModel.fieldModels) {
            classScope.updateCurrentVisibility(fieldModel.visibility, headerWriter);
            headerWriter.println(createCppFieldDeclaration(fieldModel) + ";");
            if (fieldModel._static) {
                cppWriter.println(createCppFieldDefinition(classModel, fieldModel) + ";");
            }
        }
        headerWriter.println();
        for (ConstructorModel constructorModel : classModel.constructorModels) {
            classScope.updateCurrentVisibility(constructorModel.visibility, headerWriter);
            headerWriter.println(createCppConstructorDeclaration(classModel, constructorModel) + ";");
            cppWriter.block(createCppConstructorDefinition(classModel, constructorModel, instructionModelSerializer), () -> {
                for (InstructionModel instructionModel : constructorModel.constructorBody) {
                    if (!(instructionModel instanceof SuperCallModel)) {
                        cppWriter.println(instructionModel.accept(instructionModelSerializer) + ';');
                    }
                }
            });
            cppWriter.println();
        }
        for (MethodModel methodModel : classModel.methodModels) {
            classScope.updateCurrentVisibility(methodModel.visibility, headerWriter);
            headerWriter.println(createCppClassMethodDeclaration(mainClassModel, methodModel) + ";");
            cppWriter.block(createCppClassMethodDefinition(mainClassModel, classModel, methodModel), () -> {
                for (InstructionModel instructionModel : methodModel.methodBody) {
                    cppWriter.println(instructionModel.accept(instructionModelSerializer) + ';');
                }
            });
            cppWriter.println();
        }

        for (InterfaceModel interfaceModel : classModel.interfaces) {
            classScope.updateCurrentVisibility(interfaceModel.visibility, headerWriter);
            headerWriter.block(createCppInterface(interfaceModel), () -> {
                for (MethodModel methodModel : interfaceModel.methodModels) {
                    headerWriter.println(createCppInterfaceMethodDeclaration(classModel, methodModel) + ";");
                }
            }, ";");
            headerWriter.println();
        }

        for (ClassModel innerClass : classModel.classes) {
            classScope.updateCurrentVisibility(classModel.visibility, headerWriter);
            generateClass(instructionModelSerializer, mainClassModel, innerClass, headerWriter, cppWriter);
        }
        headerWriter.closeBlock(";");
    }

    private void generateMainClass(ClassModel mainClassModel, CodePrintWriter headerWriter, CodePrintWriter cppWriter) throws CodeGeneratorException {
        final CppInstructionModelSerializer instructionModelSerializer = new CppInstructionModelSerializer();
        headerWriter.println("// GENERATED BY METAGEN");
        headerWriter.println("#pragma once");
        headerWriter.println();
        headerWriter.println("#include <memory>");
        headerWriter.println("#include <string>");
        headerWriter.println("#include <vector>");
        for (ClassModel requiredClass : mainClassModel.requiredClasses) {
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
        generateClass(instructionModelSerializer, mainClassModel, mainClassModel, headerWriter, cppWriter);

        if (mNamespaceName != null) {
            headerWriter.println("}");
            headerWriter.println();
        }
    }

    private String createCppInterface(InterfaceModel interfaceModel) {
        return "class " + interfaceModel.name;
    }

    private String createCppClass(ClassModel classModel) {
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

    private static String createCppInterfaceMethodDeclaration(ClassModel containerClass, MethodModel model) {
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

    private static String createCppClassMethodDeclaration(ClassModel containerClass, MethodModel model) {
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

    private static String createCppClassMethodDefinition(ClassModel containerClass, ClassModel classModel, MethodModel model) {
        StringBuilder ret = new StringBuilder();
        ret.append(createCppType(model.returnType));
        ret.append(' ');
        if (classModel.containerClass != null) {
            ret.append(classModel.containerClass.name);
            ret.append("::");
        }
        ret.append(classModel.name);
        ret.append("::");
        ret.append(model.name);
        StringJoiner arguments = new StringJoiner(", ", "(", ")");
        for (ArgumentModel argumentModel : model.argumentModels) {
            arguments.add(createCppArgument(argumentModel));
        }
        ret.append(arguments.toString());
        return ret.toString();
    }

    private static String createCppConstructorDeclaration(ClassModel containerClass, ConstructorModel model) {
        StringBuilder ret = new StringBuilder();
        ret.append(model.classModel.name);
        StringJoiner arguments = new StringJoiner(", ", "(", ")");
        for (ArgumentModel argumentModel : model.argumentModels) {
            arguments.add(createCppArgument(argumentModel));
        }
        ret.append(arguments.toString());
        return ret.toString();
    }

    private static String createCppConstructorDefinition(ClassModel containerClass, ConstructorModel model, CppInstructionModelSerializer serializer) {
        StringBuilder ret = new StringBuilder();
        if (model.classModel.containerClass != null) {
            ret.append(model.classModel.containerClass.name);
            ret.append("::");
        }
        ret.append(model.classModel.name);
        ret.append("::");
        ret.append(model.classModel.name);
        StringJoiner arguments = new StringJoiner(", ", "(", ")");
        for (ArgumentModel argumentModel : model.argumentModels) {
            arguments.add(createCppArgument(argumentModel));
        }
        ret.append(arguments.toString());

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
        if (argumentModel.vararg || argumentModel.array) {
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

    private static String createCppFieldDefinition(ClassModel classModel, FieldModel fieldModel) {
        StringBuilder ret = new StringBuilder();
        ret.append(createCppType(fieldModel.type));
        ret.append(' ');
        if (classModel.containerClass != null) {
            ret.append(classModel.containerClass.name);
            ret.append("::");
        }
        ret.append(classModel.name);
        ret.append("::");
        ret.append(fieldModel.name);
        return ret.toString();
    }

    private static String createCppType(TypeModel type) {
        if (type == TypeModel.TYPE_VOID) {
            return "void";
        } else if (type == TypeModel.TYPE_STRING) {
            return "std::string";
        } else {
            String ret;
            if (type.name != null) {
                ret = type.name;
            } else {
                ClassModel classModel = type.classModel;
                StringBuilder typeStringBuilder = new StringBuilder(classModel.name);
                classModel = classModel.containerClass;
                while (classModel != null) {
                    typeStringBuilder.insert(0, "::");
                    typeStringBuilder.insert(0, classModel.name);
                    classModel = classModel.containerClass;
                }
                ret = typeStringBuilder.toString();
            }
            if (type.reference) {
                return "std::shared_ptr<" + ret + ">";
            } else {
                return ret;
            }
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
                ret.append(methodCallModel.classInstance.containerClass.name).append("::");
                ret.append(methodCallModel.classInstance.name).append("::");
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

