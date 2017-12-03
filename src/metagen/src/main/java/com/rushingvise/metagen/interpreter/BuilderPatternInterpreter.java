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
import com.rushingvise.metagen.generator.CodeModel.*;
import com.rushingvise.metagen.parser.GraphsModel;
import com.rushingvise.metagen.parser.GraphsModel.*;

import java.util.*;

import static com.rushingvise.metagen.parser.GraphsModel.Utils.findNamedItem;

/**
 * Interprets the given graph as a builder.
 * Can be used to generate an API of any kind of expression builder.
 * Each node is treated as an interface that exposes methods that are available at a given phase of building the intended object.
 * Each edge is treated as a transition between builder phases.
 * Each action is treated as a generic method available at a defined builder phase, yielding any type of result.
 *
 * Three main classes are generated for each of the graphs:
 * - Builder class - class intended for the end user.
 * - API class - defines the interfaces available at each phase of building the intended object.
 * - Implementation class - class in which the logic of the builder should be placed.
 */
public class BuilderPatternInterpreter extends GraphInterpreter {
    public BuilderPatternInterpreter(GraphsModel graphsModel) {
        super(graphsModel);
    }

    @Override
    public CodeModel analyze() throws GraphInterpreterException {
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


    private List<MainClassModel> analyzeGraph(GraphModel model) throws GraphInterpreterException {
        List<MainClassModel> ret = new ArrayList<>();

        ImplementationModel implementationModel = createImplementationModel(model);
        final MainClassModel implementationClass = implementationModel.implementationClass;
        final MainClassModel apiClass = createApi(model, implementationModel);
        final MainClassModel mainClass = createBuilderClass(model, implementationModel);

        mainClass.requiredClasses.add(apiClass);
        mainClass.requiredClasses.add(implementationClass);
        apiClass.requiredClasses.add(implementationClass);

        ret.add(implementationClass);
        ret.add(apiClass);
        ret.add(mainClass);

        return ret;
    }

    private MainClassModel createApi(GraphModel model, ImplementationModel implementationModel) throws GraphInterpreterException {
        MainClassModel apiClass = new MainClassModel(model.name + "Api");
        final Map<String, InterfaceModel> interfaces = new HashMap<>();
        final TypeModel contentClassType = new TypeModel(implementationModel.contentClass);
        final FieldModel contentField = new FieldModel(contentClassType, "content");
        contentField.visibility = Visibility.PRIVATE;
        final CodeModel.ArgumentModel contentMethodArgument = new CodeModel.ArgumentModel(contentField.type, contentField.name);
        final CodeModel.ArgumentModel contentConstructorArgument = new CodeModel.ArgumentModel(contentField.type, "_" + contentField.name);

        // We should use single instance of InnerClassModel for each created class.
        final Map<String, InnerClassModel> classesCache = new HashMap<>();

        // Defining interface models for transitions between builder phases.
        for (EdgeModel edgeModel : model.edges) {
            InterfaceModel interfaceModel = new InterfaceModel(convertName(edgeModel), apiClass);
            NodeModel targetNode = findNamedItem(model.nodes, edgeModel.target);
            String targetStateName = convertName(targetNode);
            InnerClassModel targetStateClass = classesCache.get(targetStateName);
            if (targetStateClass == null) {
                targetStateClass = new InnerClassModel(targetStateName, apiClass);
                classesCache.put(targetStateClass.name, targetStateClass);
            }
            for (SignatureModel signatureModel : edgeModel.signatures) {
                MethodModel methodModel = convertTransitionSignature(signatureModel, targetStateClass);
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

        // Defining interface models for generic actions.
        for (ActionModel actionModel : model.actions) {
            InterfaceModel interfaceModel = new InterfaceModel(convertName(actionModel), apiClass);
            for (SignatureModel signatureModel : actionModel.signatures) {
                MethodModel methodModel = convertTransitionSignature(signatureModel);
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

        // Creating classes for phases of the building process.
        for (NodeModel nodeModel : model.nodes) {
            String className = convertName(nodeModel);
            InnerClassModel classModel = classesCache.get(className);
            if (classModel == null) {
                classModel = new InnerClassModel(className, apiClass);
                classesCache.put(classModel.name, classModel);
            }

            if (nodeModel.name.equals(model.initialNode)) {
                implementationModel.initialClass = classModel;
            }

            ConstructorModel constructorModel = new ConstructorModel(classModel);
            constructorModel.argumentModels.add(contentConstructorArgument);
            constructorModel.constructorBody.add(new AssignmentModel(new VariableModel(contentField),
                    new VariableModel(contentConstructorArgument.type, contentConstructorArgument.name)));
            classModel.constructorModels.add(constructorModel);

            // Class should implement actions that were included in the node definition.
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
            // Class should implement transitions that were included in the node definition.
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
            apiClass.innerClasses.add(classModel);
        }
        return apiClass;
    }

    private MethodCallModel createLogicMethodCall(InterfaceModel interfaceModel, InnerClassModel logicClassModel, MethodModel methodModel, FieldModel contentField) {
        return new MethodCallModel(
                logicClassModel,
                getLogicMethodName(interfaceModel.name, methodModel.name),
                forwardMethodArguments(methodModel.argumentModels, new VariableModel(contentField))
        );
    }

    private MainClassModel createBuilderClass(GraphModel model, ImplementationModel implementationModel) throws GraphInterpreterException {
        MainClassModel apiClass = new MainClassModel(model.name);
        ConstructorModel constructorModel = new ConstructorModel(apiClass);
        SuperCallModel superCallModel = new SuperCallModel(implementationModel.initialClass, Arrays.asList(new AllocationModel(new TypeModel(implementationModel.contentClass))));
        constructorModel.constructorBody.add(superCallModel);
        apiClass.constructorModels.add(constructorModel);
        apiClass.superClass = implementationModel.initialClass;
        return apiClass;
    }

    private static ImplementationModel createImplementationModel(GraphModel model) {
        MainClassModel implementationClass = new MainClassModel(model.name + "Impl");
        implementationClass.template = true;
        InnerClassModel contentClass = new InnerClassModel("Content", implementationClass);
        InnerClassModel logicClass = new InnerClassModel("Logic", implementationClass);
        implementationClass.innerClasses.add(contentClass);
        implementationClass.innerClasses.add(logicClass);

        ImplementationModel implementationModel = new ImplementationModel();
        implementationModel.implementationClass = implementationClass;
        implementationModel.contentClass = contentClass;
        implementationModel.logicClass = logicClass;

        return implementationModel;
    }

    private static class ImplementationModel {
        MainClassModel implementationClass;
        InnerClassModel logicClass;
        InnerClassModel contentClass;
        InnerClassModel initialClass;
    }

    private static MethodModel convertTransitionSignature(SignatureModel signatureModel) throws GraphInterpreterException {
        MethodModel ret = new MethodModel(signatureModel.name);

        ret.returnType = convertType(signatureModel.returnType);
        if (signatureModel.arguments != null) {
            for (GraphsModel.ArgumentModel argumentModel : signatureModel.arguments) {
                CodeModel.ArgumentModel codeArgumentModel = new CodeModel.ArgumentModel(convertType(argumentModel.type), argumentModel.name);
                codeArgumentModel.array = argumentModel.array;
                codeArgumentModel.variadic = argumentModel.vararg;
                ret.argumentModels.add(codeArgumentModel);
            }
        }
        return ret;
    }

    private static TypeModel convertType(String type) {
        if ("string".equals(type)) {
            return TypeModel.TYPE_STRING;
        } else {
            // TODO: resolve external classes properly, throw new GraphInterpreterException("Unsupported type: " + type);
            return new TypeModel(type);
        }
    }

    private static MethodModel convertTransitionSignature(SignatureModel signatureModel, EntityModel returnType) throws GraphInterpreterException {
        MethodModel ret = new MethodModel(signatureModel.name);

        ret.returnType = new TypeModel(returnType);
        if (signatureModel.arguments != null) {
            for (GraphsModel.ArgumentModel argumentModel : signatureModel.arguments) {
                CodeModel.ArgumentModel codeArgumentModel = new CodeModel.ArgumentModel(convertType(argumentModel.type), argumentModel.name);
                codeArgumentModel.array = argumentModel.array;
                codeArgumentModel.variadic = argumentModel.vararg;
                ret.argumentModels.add(codeArgumentModel);
            }
        }
        return ret;
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
