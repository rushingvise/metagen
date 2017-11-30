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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeModel {
    public List<MainClassModel> classes = new ArrayList<>();

    public static abstract class EntityModel {
        public String name;
        public List<MethodModel> methodModels = new ArrayList<>();
        public Visibility visibility = Visibility.PUBLIC;

        public EntityModel(String name) {
            this.name = name;
        }
    }

    public interface InnerEntityModel {
        MainClassModel getOuterClass();
    }

    public static class InterfaceModel extends EntityModel implements InnerEntityModel {
        public MainClassModel outerClass;

        public InterfaceModel(String name, MainClassModel outerClass) {
            super(name);
            this.outerClass = outerClass;
        }

        @Override
        public MainClassModel getOuterClass() {
            return outerClass;
        }
    }

    // TODO: rename back to ClassModel
    public static abstract class AbstractClassModel extends EntityModel {
        public AbstractClassModel superClass;
        public List<ConstructorModel> constructorModels = new ArrayList<>();
        public List<FieldModel> fieldModels = new ArrayList<>();
        public List<InterfaceModel> implementedInterfaceModels = new ArrayList<>();

        public AbstractClassModel(String name) {
            super(name);
        }
    }

    public static class MainClassModel extends AbstractClassModel {
        public List<MainClassModel> requiredClasses = new ArrayList<>();

        public List<InnerClassModel> innerClasses = new ArrayList<>();
        public List<InterfaceModel> interfaces = new ArrayList<>();

        public MainClassModel(String name) {
            super(name);
        }
    }

    public static class InnerClassModel extends AbstractClassModel implements InnerEntityModel {
        public MainClassModel outerClass;

        public InnerClassModel(String name, MainClassModel outerClass) {
            super(name);
            this.outerClass = outerClass;
        }

        @Override
        public MainClassModel getOuterClass() {
            return outerClass;
        }
    }

    public static class ConstructorModel {
        public AbstractClassModel classModel;
        public Visibility visibility = Visibility.PUBLIC;
        public List<ArgumentModel> argumentModels = new ArrayList<>();
        public List<InstructionModel> constructorBody = new ArrayList<>();

        public ConstructorModel(AbstractClassModel classModel) {
            this.classModel = classModel;
        }
    }

    public static class MethodModel {
        public String name;
        public TypeModel returnType = TypeModel.TYPE_VOID;
        public boolean _static;
        public boolean overrides;
        public Visibility visibility = Visibility.PUBLIC;
        public List<ArgumentModel> argumentModels = new ArrayList<>();
        public List<InstructionModel> methodBody = new ArrayList<>();

        public MethodModel(String name) {
            this.name = name;
        }

        public MethodModel copy() {
            MethodModel ret = new MethodModel(this.name);
            ret.returnType = this.returnType;
            ret._static = this._static;
            ret.overrides = this.overrides;
            ret.visibility = this.visibility;
            ret.argumentModels.addAll(this.argumentModels);
            ret.methodBody.addAll(this.methodBody);
            return ret;
        }
    }

    public enum Visibility {
        PUBLIC,
        PROTECTED,
        PRIVATE
    }

    public static class FieldModel {
        public String name;
        public TypeModel type;
        public boolean _static;
        public Visibility visibility = Visibility.PUBLIC;

        public FieldModel(TypeModel type, String name) {
            this.name = name;
            this.type = type;
        }
    }

    public static class ArgumentModel {
        public String name;
        public TypeModel type;
        public boolean vararg;
        public boolean array;

        public ArgumentModel(TypeModel type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    public static class TypeModel {
        public static final TypeModel TYPE_STRING = new TypeModel("string");
        public static final TypeModel TYPE_VOID = new TypeModel("void");

        public final EntityModel entityModel;
        public final String name;
        public boolean reference;

        public TypeModel(String name) {
            this.name = name;
            this.entityModel = null;
        }

        public TypeModel(EntityModel entityModel) {
            this.name = null;
            this.entityModel = entityModel;
        }
    }

    public interface InstructionModel {
        String accept(CodeGenerator.InstructionModelSerializer visitor);
    }

    public interface RValueModel extends InstructionModel {
    }

    public interface LValueModel extends RValueModel {
    }

    public static class StringValueModel implements RValueModel {
        public String value;

        public StringValueModel(String value) {
            this.value = value;
        }

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    public static class IntegerValueModel implements RValueModel {
        public Integer value;

        public IntegerValueModel(Integer value) {
            this.value = value;
        }

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    public static class NullValueModel implements RValueModel {
        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    public static class VariableModel implements LValueModel {
        public TypeModel type;
        public String name;

        public VariableModel(TypeModel type, String name) {
            this.type = type;
            this.name = name;
        }

        public VariableModel(FieldModel fieldModel) {
            this.type = fieldModel.type;
            this.name = fieldModel.name;
        }

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    public static class DeclarationModel implements InstructionModel {
        public VariableModel variable;

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    public static class AssignmentModel implements InstructionModel {
        public LValueModel leftValue;
        public RValueModel rightValue;

        public AssignmentModel(LValueModel to, RValueModel from) {
            leftValue = to;
            rightValue = from;
        }

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    public static class ReturnInstructionModel implements InstructionModel {
        public RValueModel returnedStatement;

        public ReturnInstructionModel(RValueModel returnedStatement) {
            this.returnedStatement = returnedStatement;
        }

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    public static class MethodCallModel implements RValueModel {
        public final VariableModel instance;
        public final AbstractClassModel classInstance;
        public String methodName;
        public List<RValueModel> parameters = new ArrayList<>();

        public MethodCallModel(VariableModel instance, String methodName, List<RValueModel> parameters) {
            this.instance = instance;
            this.classInstance = null;
            this.methodName = methodName;
            this.parameters.addAll(parameters);
        }

        public MethodCallModel(AbstractClassModel instance, String methodName, List<RValueModel> parameters) {
            this.instance = null;
            this.classInstance = instance;
            this.methodName = methodName;
            this.parameters.addAll(parameters);
        }

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    public static class SuperCallModel implements InstructionModel {
        public final AbstractClassModel superClass;
        public List<RValueModel> parameters = new ArrayList<>();

        public SuperCallModel(AbstractClassModel superClass, List<RValueModel> parameters) {
            this.superClass = superClass;
            this.parameters.addAll(parameters);
        }

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    public static class AllocationModel implements RValueModel {
        public TypeModel type;
        public List<RValueModel> parameters = new ArrayList<>();

        public AllocationModel(TypeModel type) {
            this.type = type;
        }

        public AllocationModel(TypeModel type, RValueModel... parameters) {
            this.type = type;
            this.parameters.addAll(Arrays.asList(parameters));
        }

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }
}
