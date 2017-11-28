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

package com.rushingvise.metagen.compiler;

import com.rushingvise.metagen.generator.CodeModel;
import com.rushingvise.metagen.generator.CodeModel.*;
import com.rushingvise.metagen.parser.GraphsModel;
import com.rushingvise.metagen.parser.GraphsModel.*;

import java.util.*;

import static com.rushingvise.metagen.parser.GraphsModel.Utils.findNamedItem;

public class BuilderPatternCompiler extends GraphCompiler {
    public BuilderPatternCompiler(GraphsModel graphsModel) {
        super(graphsModel);
    }

    @Override
    public CodeModel analyze() throws GraphCompilerException {
        CodeModel ret = new CodeModel();
        for (GraphModel model : mGraphsModel.graphs) {
            ret.classes.addAll(analyzeGraph(model));
        }
        return ret;
    }

    protected String convertName(IncludeEdgeModel edgeModel) {
        return "I" + edgeModel.getName() + "Transition";
    }

    protected String convertName(EdgeModel edgeModel) {
        return "I" + edgeModel.getName() + "Transition";
    }

    protected String convertName(IncludeActionModel actionModel) {
        return "I" + actionModel.getName() + "Transformation";
    }

    protected String convertName(ActionModel actionModel) {
        return "I" + actionModel.getName() + "Transformation";
    }

    protected String convertName(NodeModel nodeModel) {
        return nodeModel.getName() + "Step";
    }

    protected String getLogicMethodName(String interfaceName, String methodName) {
        return Character.toLowerCase(interfaceName.charAt(1)) + interfaceName.substring(2)
                + Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
    }


    private List<ClassModel> analyzeGraph(GraphModel model) throws GraphCompilerException {
        List<ClassModel> ret = new ArrayList<>();

        ImplementationModel implementationModel = createImplementationModel(model);
        final ClassModel implementationClass = implementationModel.implementationClass;
        final ClassModel apiClass = createApi(model, implementationModel);
        final ClassModel mainClass = createMainClass(model, implementationModel);

        mainClass.requiredClasses.add(apiClass);
        mainClass.requiredClasses.add(implementationClass);
        apiClass.requiredClasses.add(implementationClass);

        ret.add(implementationClass);
        ret.add(apiClass);
        ret.add(mainClass);

        return ret;
    }

    private ClassModel createApi(GraphModel model, ImplementationModel implementationModel) throws GraphCompilerException {
        ClassModel apiClass = new ClassModel(model.name + "Api");
        final Map<String, InterfaceModel> interfaces = new HashMap<>();
        final TypeModel contentClassType = new TypeModel(implementationModel.contentClass);
        final FieldModel contentField = new FieldModel(contentClassType, "content");
        contentField.visibility = Visibility.PRIVATE;
        final CodeModel.ArgumentModel contentMethodArgument = new CodeModel.ArgumentModel(contentField.type, contentField.name);
        final CodeModel.ArgumentModel contentConstructorArgument = new CodeModel.ArgumentModel(contentField.type, "_" + contentField.name);

        for (EdgeModel edgeModel : model.edges) {
            InterfaceModel interfaceModel = new InterfaceModel(convertName(edgeModel));
            NodeModel targetNode = findNamedItem(model.nodes, edgeModel.target);
            String targetStateName = convertName(targetNode);
            for (SignatureModel signatureModel : edgeModel.signatures) {
                MethodModel methodModel = convertSignature(signatureModel, targetStateName);
                interfaceModel.methodModels.add(methodModel);

                MethodModel logicMethodModel = methodModel.copy();
                logicMethodModel.name = getLogicMethodName(interfaceModel.name, methodModel.name);
                logicMethodModel.argumentModels.add(0, contentMethodArgument);
                logicMethodModel._static = true;
                logicMethodModel.returnType = TypeModel.TYPE_VOID;
                implementationModel.logicClass.methodModels.add(logicMethodModel);
            }
            interfaces.put(interfaceModel.name, interfaceModel);
            apiClass.interfaces.add(interfaceModel);
        }

        for (ActionModel actionModel : model.actions) {
            InterfaceModel interfaceModel = new InterfaceModel(convertName(actionModel));
            for (SignatureModel signatureModel : actionModel.signatures) {
                MethodModel methodModel = convertSignature(signatureModel);
                interfaceModel.methodModels.add(methodModel);

                MethodModel logicMethodModel = methodModel.copy();
                logicMethodModel.name = getLogicMethodName(interfaceModel.name, methodModel.name);
                logicMethodModel.argumentModels.add(0, contentMethodArgument);
                logicMethodModel._static = true;
                if (logicMethodModel.returnType != TypeModel.TYPE_VOID) {
                    logicMethodModel.methodBody.add(new ReturnInstructionModel(new NullValueModel()));
                }
                implementationModel.logicClass.methodModels.add(logicMethodModel);
            }
            interfaces.put(interfaceModel.name, interfaceModel);
            apiClass.interfaces.add(interfaceModel);
        }

        for (NodeModel nodeModel : model.nodes) {
            ClassModel classModel = new ClassModel(convertName(nodeModel), apiClass);
            classModel._static = true;

            if (nodeModel.name.equals(model.initialNode)) {
                implementationModel.initialClass = classModel;
            }

            ConstructorModel constructorModel = new ConstructorModel(classModel);
            constructorModel.argumentModels.add(contentConstructorArgument);
            constructorModel.constructorBody.add(new AssignmentModel(new VariableModel(contentField),
                    new VariableModel(contentConstructorArgument.type, contentConstructorArgument.name)));
            classModel.constructorModels.add(constructorModel);

            if (nodeModel.includedActions != null) {
                for (IncludeActionModel includeActionModel : nodeModel.includedActions) {
                    InterfaceModel interfaceModel = interfaces.get(convertName(includeActionModel));
                    classModel.implementedInterfaceModels.add(interfaceModel);

                    for (MethodModel methodModel : interfaceModel.methodModels) {
                        MethodModel implementedMethodModel = methodModel.copy();
                        implementedMethodModel.overrides = true;
                        MethodCallModel methodCallModel = createLogicMethodCall(interfaceModel, implementationModel.logicClass, methodModel, contentField);
                        if (methodModel.returnType != null) {
                            implementedMethodModel.methodBody.add(new ReturnInstructionModel(methodCallModel));
                        } else {
                            implementedMethodModel.methodBody.add(methodCallModel);
                        }
                        classModel.methodModels.add(implementedMethodModel);
                    }
                }
            }
            if (nodeModel.includedEdges != null) {
                for (IncludeEdgeModel includeEdgeModel : nodeModel.includedEdges) {
                    InterfaceModel interfaceModel = interfaces.get(convertName(includeEdgeModel));
                    classModel.implementedInterfaceModels.add(interfaceModel);

                    for (MethodModel methodModel : interfaceModel.methodModels) {
                        MethodModel implementedMethodModel = methodModel.copy();
                        implementedMethodModel.overrides = true;
                        implementedMethodModel.methodBody.add(
                                createLogicMethodCall(interfaceModel, implementationModel.logicClass, methodModel, contentField)
                        );
                        implementedMethodModel.methodBody.add(
                                new ReturnInstructionModel(
                                        new AllocationModel(
                                                implementedMethodModel.returnType,
                                                new VariableModel(contentField)
                                        )
                                )
                        );
                        classModel.methodModels.add(implementedMethodModel);
                    }
                }
            }
            classModel.fieldModels.add(contentField);
            apiClass.classes.add(classModel);
        }
        return apiClass;
    }

    private MethodCallModel createLogicMethodCall(InterfaceModel interfaceModel, ClassModel logicClassModel, MethodModel methodModel, FieldModel contentField) {
        return new MethodCallModel(
                logicClassModel,
                getLogicMethodName(interfaceModel.name, methodModel.name),
                forwardMethodArguments(methodModel.argumentModels, new VariableModel(contentField))
        );
    }

    private ClassModel createMainClass(GraphModel model, ImplementationModel implementationModel) throws GraphCompilerException {
        ClassModel apiClass = new ClassModel(model.name);
        ConstructorModel constructorModel = new ConstructorModel(apiClass);
        SuperCallModel superCallModel = new SuperCallModel(implementationModel.initialClass, Arrays.asList(new AllocationModel(new TypeModel(implementationModel.contentClass))));
        constructorModel.constructorBody.add(superCallModel);
        apiClass.constructorModels.add(constructorModel);
        apiClass.superClass = implementationModel.initialClass;
        return apiClass;
    }

    private static ImplementationModel createImplementationModel(GraphModel model) {
        ClassModel implementationClass = new ClassModel(model.name + "Impl");
        ClassModel contentClass = new ClassModel("Content", implementationClass);
        contentClass._static = true;
        ClassModel logicClass = new ClassModel("Logic", implementationClass);
        logicClass._static = true;
        implementationClass.classes.add(contentClass);
        implementationClass.classes.add(logicClass);

        ImplementationModel implementationModel = new ImplementationModel();
        implementationModel.implementationClass = implementationClass;
        implementationModel.contentClass = contentClass;
        implementationModel.logicClass = logicClass;

        return implementationModel;
    }

    private static class ImplementationModel {
        ClassModel implementationClass;
        ClassModel logicClass;
        ClassModel contentClass;
        ClassModel initialClass;
    }

    private static MethodModel convertSignature(SignatureModel signatureModel) throws GraphCompilerException {
        return convertSignature(signatureModel, signatureModel.returnType);
    }

    private static TypeModel convertType(String type) {
        if ("string".equals(type)) {
            return TypeModel.TYPE_STRING;
        } else {
            // TODO: resolve external classes properly, throw new GraphCompilerException("Unsupported type: " + type);
            return new TypeModel(type);
        }
    }

    private static MethodModel convertSignature(SignatureModel signatureModel, String returnType) throws GraphCompilerException {
        MethodModel ret = new MethodModel(signatureModel.name);

        ret.returnType = convertType(returnType);
        if (signatureModel.arguments != null) {
            for (GraphsModel.ArgumentModel argumentModel : signatureModel.arguments) {
                CodeModel.ArgumentModel codeArgumentModel = new CodeModel.ArgumentModel(convertType(argumentModel.type), argumentModel.name);
                codeArgumentModel.array = argumentModel.array;
                codeArgumentModel.vararg = argumentModel.vararg;
                ret.argumentModels.add(codeArgumentModel);
            }
        }
        return ret;
    }

    private List<RValueModel> forwardMethodArguments(List<CodeModel.ArgumentModel> argumentModels) {
        return forwardMethodArguments(argumentModels, null);
    }

    private List<RValueModel> forwardMethodArguments(List<CodeModel.ArgumentModel> argumentModels, VariableModel prefixVariable) {
        List<RValueModel> ret = new ArrayList<>(argumentModels.size());
        if (prefixVariable != null) {
            ret.add(prefixVariable);
        }
        for (CodeModel.ArgumentModel argumentModel : argumentModels) {
            ret.add(new VariableModel(argumentModel.type, argumentModel.name));
        }
        return ret;
    }
}
