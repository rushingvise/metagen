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
 * Creates java classes based on the provided {@link CodeModel}.
 */
public class JavaCodeGenerator extends CodeGenerator {
    private final String mPackageName;

    /**
     * Generates java code in the given output path.
     * Please note that output path should take the package name into account.
     * @param outputPath Directory in which the java files should be created.
     * @param codeModel Model for which the code should be generated.
     * @param packageName Package name that will be used in the generated java files.
     */
    public JavaCodeGenerator(String outputPath, CodeModel codeModel, String packageName) {
        super(outputPath, codeModel);
        mPackageName = packageName;
    }

    @Override
    public void generate() throws CodeGeneratorException {
        try {
            for (MainClassModel classModel : mCodeModel.classes) {
                File outputFile = new File(mOutputPath, classModel.name + ".java" + (classModel.template ? ".template" : ""));
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                }
                FileOutputStream outputStream = new FileOutputStream(outputFile);
                CodePrintWriter writer = new CodePrintWriter(outputStream);
                generateMainClass(classModel, writer);
            }
        } catch (IOException e) {
            throw new CodeGeneratorException(e);
        }
    }

    private void generateClassBody(JavaInstructionModelSerializer instructionModelSerializer, AbstractClassModel classModel, CodePrintWriter writer) throws CodeGeneratorException {
        // Declaring fields defined in this class model.
        for (FieldModel fieldModel : classModel.fieldModels) {
            writer.println(createJavaVisibility(fieldModel.visibility) + " " + createJavaField(fieldModel) + ";");
        }
        writer.println();
        // Declaring constructors defined in this class model.
        for (ConstructorModel constructorModel : classModel.constructorModels) {
            writer.block(createJavaConstructor(constructorModel), () -> {
                for (InstructionModel instructionModel : constructorModel.constructorBody) {
                    writer.println(instructionModel.accept(instructionModelSerializer) + ';');
                }
            });
            writer.println();
        }
        // Declaring methods defined in this class model.
        for (MethodModel methodModel : classModel.methodModels) {
            writer.block(createJavaClassMethod(methodModel), () -> {
                for (InstructionModel instructionModel : methodModel.methodBody) {
                    writer.println(instructionModel.accept(instructionModelSerializer) + ';');
                }
            });
            writer.println();
        }
    }

    private void generateMainClass(MainClassModel mainClassModel, CodePrintWriter writer) throws CodeGeneratorException {
        final JavaInstructionModelSerializer instructionModelSerializer = new JavaInstructionModelSerializer();
        writer.println("// GENERATED BY METAGEN");
        writer.println();

        // Declaring package name.
        if (mPackageName != null) {
            writer.println("package " + mPackageName + ";");
            writer.println();
        }

        // Declaring the top level class.
        writer.block(createJavaClass(mainClassModel), () -> {
            // Generating class body.
            generateClassBody(instructionModelSerializer, mainClassModel, writer);

            // Declaring interfaces defined in this main class model.
            for (InterfaceModel interfaceModel : mainClassModel.interfaces) {
                writer.block(createJavaInterface(interfaceModel), () -> {
                    for (MethodModel methodModel : interfaceModel.methodModels) {
                        writer.println(createJavaInterfaceMethod(methodModel) + ";");
                    }
                });
                writer.println();
            }

            // Declaring inner classes defined in this main class model.
            for (InnerClassModel innerClass : mainClassModel.innerClasses) {
                writer.block(createJavaClass(innerClass), () -> {
                    generateClassBody(instructionModelSerializer, innerClass, writer);
                });
            }
        });
    }

    private String createJavaInterface(InterfaceModel interfaceModel) {
        return createJavaVisibility(interfaceModel.visibility) + " interface " + interfaceModel.name;
    }

    private String createJavaClass(AbstractClassModel classModel) {
        StringBuilder ret = new StringBuilder();
        ret.append(createJavaVisibility(classModel.visibility));
        ret.append(' ');
        if (classModel instanceof InnerEntityModel) {
            ret.append("static ");
        }
        ret.append("class ");
        ret.append(classModel.name);
        ret.append(' ');
        if (classModel.superClass != null) {
            ret.append("extends ");
            ret.append(createJavaQualifiedClassName(classModel.superClass));
        }
        if (classModel.implementedInterfaceModels.size() > 0) {
            StringJoiner interfaces = new StringJoiner(", ", "implements ", "");
            for (InterfaceModel interfaceModel : classModel.implementedInterfaceModels) {
                interfaces.add(interfaceModel.name);
            }
            ret.append(interfaces.toString());
        }
        return ret.toString();
    }

    private static String createJavaVisibility(Visibility visibility) {
        switch (visibility) {
            case PUBLIC:
            default:
                return "public";
            case PRIVATE:
                return "private";
            case PROTECTED:
                return "protected";
        }
    }

    private static String createJavaInterfaceMethod(MethodModel model) {
        StringBuilder ret = new StringBuilder();
        ret.append(createJavaType(model.returnType));
        ret.append(' ');
        ret.append(model.name);
        StringJoiner arguments = new StringJoiner(", ", "(", ")");
        for (ArgumentModel argumentModel : model.argumentModels) {
            arguments.add(createJavaArgument(argumentModel));
        }
        ret.append(arguments.toString());
        return ret.toString();
    }

    private static String createJavaClassMethod(MethodModel model) {
        StringBuilder ret = new StringBuilder();
        if (model.overrides) {
            ret.append("@Override\n");
        }
        ret.append(createJavaVisibility(model.visibility));
        ret.append(' ');
        if (model._static) {
            ret.append("static ");
        }
        ret.append(createJavaType(model.returnType));
        ret.append(' ');
        ret.append(model.name);
        StringJoiner arguments = new StringJoiner(", ", "(", ")");
        for (ArgumentModel argumentModel : model.argumentModels) {
            arguments.add(createJavaArgument(argumentModel));
        }
        ret.append(arguments.toString());
        return ret.toString();
    }

    private static String createJavaConstructor(ConstructorModel model) {
        StringBuilder ret = new StringBuilder();
        ret.append(createJavaVisibility(model.visibility));
        ret.append(' ');
        ret.append(model.classModel.name);
        StringJoiner arguments = new StringJoiner(", ", "(", ")");
        for (ArgumentModel argumentModel : model.argumentModels) {
            arguments.add(createJavaArgument(argumentModel));
        }
        ret.append(arguments.toString());
        return ret.toString();
    }

    private static String createJavaArgument(ArgumentModel argumentModel) {
        StringBuilder ret = new StringBuilder();
        ret.append(createJavaType(argumentModel.type));
        if (argumentModel.variadic) {
            ret.append("...");
        } else if (argumentModel.array) {
            ret.append("[]");
        }
        ret.append(' ');
        ret.append(argumentModel.name);
        return ret.toString();
    }

    private static String createJavaField(FieldModel fieldModel) {
        StringBuilder ret = new StringBuilder();
        if (fieldModel._static) {
            ret.append("static ");
        }
        ret.append(' ');
        ret.append(createJavaType(fieldModel.type));
        ret.append(' ');
        ret.append(fieldModel.name);
        return ret.toString();
    }

    private static String createJavaType(TypeModel type) {
        if (type == TypeModel.TYPE_VOID) {
            return "void";
        } else if (type == TypeModel.TYPE_STRING) {
            return "String";
        } else if (type == TypeModel.TYPE_INTEGER) {
            return "int";
        } else if (type.name != null) {
            return type.name;
        } else {
            EntityModel entityModel = type.entityModel;
            return createJavaQualifiedClassName(entityModel);
        }
    }

    private static String createJavaQualifiedClassName(EntityModel entityModel) {
        if (entityModel instanceof InnerEntityModel) {
            return ((InnerEntityModel) entityModel).getOuterClass().name + "." + entityModel.name;
        } else {
            return entityModel.name;
        }
    }

    private static class JavaInstructionModelSerializer implements InstructionModelSerializer {
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
            return "null";
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
            return "return " + returnInstructionModel.returnedStatement.accept(this);
        }

        @Override
        public String visit(MethodCallModel methodCallModel) {
            StringBuilder ret = new StringBuilder();
            if (methodCallModel.classInstance != null) {
                ret.append(createJavaQualifiedClassName(methodCallModel.classInstance));
                ret.append('.');
            } else if (methodCallModel.instance != null) {
                ret.append(methodCallModel.instance.name).append('.');
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
            ret.append("new ");
            ret.append(createJavaType(allocationModel.type));
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
            ret.append("super");
            StringJoiner arguments = new StringJoiner(", ", "(", ")");
            for (RValueModel rValueModel : superCallModel.parameters) {
                arguments.add(rValueModel.accept(this));
            }
            ret.append(arguments.toString());
            return ret.toString();
        }
    }
}
