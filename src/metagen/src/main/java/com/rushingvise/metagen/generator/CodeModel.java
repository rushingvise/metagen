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

import java.util.*;

/**
 * Language-agnostic code descriptor.
 */
public class CodeModel {
    /**
     * Top level classes. Usually each {@link MainClassModel} will result in a separate code unit.
     */
    public List<MainClassModel> classes = new ArrayList<>();

    /**
     * Abstract class for classes/interfaces models.
     */
    public static abstract class EntityModel {
        /**
         * Name of the class/interface.
         */
        public String name;

        /**
         * Methods available in the class/interface.
         */
        public List<MethodModel> methodModels = new ArrayList<>();

        /**
         * Visibility of the class/interface.
         * Most likely not applicable to {@link MainClassModel}.
         */
        public Visibility visibility = Visibility.PUBLIC;

        public EntityModel(String name) {
            this.name = name;
        }
    }

    /**
     * Interface for entities that are defined within another entity.
     */
    public interface InnerEntityModel {
        /**
         * @return Outer (wrapping) class model.
         */
        MainClassModel getOuterClass();
    }

    /**
     * Class for interface models, that could be implemented by classes, see {@link AbstractClassModel}.
     * Can be defined only withing {@link MainClassModel}.
     */
    public static class InterfaceModel extends EntityModel implements InnerEntityModel {
        /**
         * Outer (wrapping) class model.
         */
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

    /**
     * Parent class of {@link MainClassModel} and {@link InnerClassModel}.
     */
    public static abstract class AbstractClassModel extends EntityModel {
        /**
         * Parent class which this class derives from.
         */
        public AbstractClassModel superClass;

        /**
         * Models of constructors for this class, see {@link ConstructorModel}.
         */
        public List<ConstructorModel> constructorModels = new ArrayList<>();

        /**
         * Models of fields for this class, see {@link FieldModel}.
         */
        public List<FieldModel> fieldModels = new ArrayList<>();

        /**
         * List of implemented interfaces.
         * This list is used only for generating proper declaration of the class.
         * Actual methods should be provided in {@code methodModels}, see {@link EntityModel}.
         */
        public List<InterfaceModel> implementedInterfaceModels = new ArrayList<>();

        public AbstractClassModel(String name) {
            super(name);
        }
    }

    /**
     * Main class model. Generator will create a separate code unit for such class (*.java or *.cpp & *.h files).
     * Can contain inner classes ({@link InnerClassModel} and interfaces ({@link InterfaceModel}.
     */
    public static class MainClassModel extends AbstractClassModel {
        /**
         * Other main classes on which this class relies (used for generating import or #include statements).
         */
        public Set<MainClassModel> requiredClasses = new LinkedHashSet<>();

        /**
         * Models of inner classes of this class.
         */
        public Set<InnerClassModel> innerClasses = new LinkedHashSet<>();

        /**
         * Interfaces, which should be declared within the body of this class.
         */
        public Set<InterfaceModel> interfaces = new LinkedHashSet<>();

        /**
         * Hint for the generator.
         * Indicates that this class should be implemented by the developer.
         */
        public boolean template;

        public MainClassModel(String name) {
            super(name);
        }
    }

    /**
     * Model of an inner class that can be included in {@link MainClassModel}.
     */
    public static class InnerClassModel extends AbstractClassModel implements InnerEntityModel {
        /**
         * Outer (wrapping) class model.
         */
        public MainClassModel outerClass;

        public InnerClassModel(String name, MainClassModel outerClass) {
            super(name);
            this.outerClass = outerClass;
            this.outerClass.innerClasses.add(this);
        }

        @Override
        public MainClassModel getOuterClass() {
            return outerClass;
        }
    }

    /**
     * Model of a constructor of a given class, see {@link AbstractClassModel}.
     */
    public static class ConstructorModel {
        /**
         * Class model for which this constructor is defined.
         */
        public AbstractClassModel classModel;

        /**
         * Visibility of the constructor.
         */
        public Visibility visibility = Visibility.PUBLIC;

        /**
         * Parameters of the constructor.
         */
        public List<ArgumentModel> argumentModels = new ArrayList<>();

        /**
         * Optional body of the constructor, see {@link com.rushingvise.metagen.generator.CodeGenerator.InstructionModelSerializer}.
         */
        public List<InstructionModel> constructorBody = new ArrayList<>();

        public ConstructorModel(AbstractClassModel classModel) {
            this.classModel = classModel;
        }
    }

    /**
     * Model of a method of a given class or interface, see {@link EntityModel}.
     */
    public static class MethodModel {
        /**
         * Name of the method.
         */
        public String name;

        /**
         * Return type of the method, see {@link TypeModel}.
         */
        public TypeModel returnType = TypeModel.TYPE_VOID;

        /**
         * Indicates if the method should be declared as static.
         */
        public boolean _static;

        /**
         * Indicates if the method is overriding another method from parent class or implemented interfaces.
         */
        public boolean overrides;

        /**
         * Visibility of the constructor.
         */
        public Visibility visibility = Visibility.PUBLIC;

        /**
         * Parameters of the method.
         */
        public List<ArgumentModel> argumentModels = new ArrayList<>();

        /**
         * Optional body of the method, see {@link com.rushingvise.metagen.generator.CodeGenerator.InstructionModelSerializer}.
         */
        public List<InstructionModel> methodBody = new ArrayList<>();

        public MethodModel(String name) {
            this.name = name;
        }

        /**
         * @return Shallow copy of the method model.
         */
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

    /**
     * Enum describing visibility of a method, class, field etc.
     */
    public enum Visibility {
        PUBLIC,
        PROTECTED,
        PRIVATE
    }

    /**
     * Model of a field of a given class, see {@link AbstractClassModel}.
     */
    public static class FieldModel {
        /**
         * Name of the field.
         */
        public String name;

        /**
         * Type of the field, see {@link TypeModel}.
         */
        public TypeModel type;

        /**
         * Indicates if the field should be declared as static.
         */
        public boolean _static;

        /**
         * Visibility of the field.
         */
        public Visibility visibility = Visibility.PUBLIC;

        public FieldModel(TypeModel type, String name) {
            this.name = name;
            this.type = type;
        }
    }

    /**
     * Model of an argument that should be passed to constructor ({@link ConstructorModel} or method ({@link MethodModel}.
     */
    public static class ArgumentModel {
        /**
         * Name of the argument.
         */
        public String name;

        /**
         * Type of the argument.
         */
        public TypeModel type;

        /**
         * Indicates if the argument is variadic (accepts multiple values of the defined type).
         */
        public boolean variadic;

        /**
         * Indicates if the argument is an array.
         */
        public boolean array;

        public ArgumentModel(TypeModel type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    /**
     * Model of a type. Can wrap defined entity model or an external type.
     */
    public static class TypeModel {
        /**
         * Language-agnostic string type.
         */
        public static final TypeModel TYPE_STRING = new TypeModel("string");

        /**
         * Language-agnostic string type.
         */
        public static final TypeModel TYPE_INTEGER = new TypeModel("integer");

        /**
         * Language-agnostic void type.
         */
        public static final TypeModel TYPE_VOID = new TypeModel("void");

        /**
         * Entity defined within the {@link CodeModel}.
         */
        public final EntityModel entityModel;

        /**
         * External type.
         */
        public final String name;

        /**
         * Indicates if the type should be declared as a reference.
         */
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

    /**
     * Visitor pattern interface used for generating constructor/method bodies.
     */
    public interface InstructionModel {
        String accept(CodeGenerator.InstructionModelSerializer visitor);
    }

    /**
     * Parent interface for statements returning r-values.
     */
    public interface RValueModel extends InstructionModel {
    }

    /**
     * Parent interface for statements returning l-values.
     */
    public interface LValueModel extends RValueModel {
    }

    /**
     * Statement returning string instance with a given value.
     */
    public static class StringValueModel implements RValueModel {
        /**
         * Value with which the string should be constructed.
         */
        public String value;

        public StringValueModel(String value) {
            this.value = value;
        }

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Statement returning integer value.
     */
    public static class IntegerValueModel implements RValueModel {
        /**
         * Value of the integer that should be returned.
         */
        public Integer value;

        public IntegerValueModel(Integer value) {
            this.value = value;
        }

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Language-agnostic null value.
     */
    public static class NullValueModel implements RValueModel {
        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Statement returning a variable. See {@link AssignmentModel} and {@link DeclarationModel}.
     */
    public static class VariableModel implements LValueModel {
        /**
         * Type of the variable.
         */
        public TypeModel type;

        /**
         * Name of the variable.
         */
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

    /**
     * Declares a variable based on a given model.
     */
    public static class DeclarationModel implements InstructionModel {
        public VariableModel variable;

        @Override
        public String accept(CodeGenerator.InstructionModelSerializer visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Assigns r-value to an l-value instance.
     */
    public static class AssignmentModel implements InstructionModel {
        /**
         * L-value to which assignment is done.
         */
        public LValueModel leftValue;
        /**
         * R-value which is assigned to the l-value.
         */
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

    /**
     * Statement returning value from the current method.
     */
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

    /**
     * Class or instance method call. Can return an r-value.
     */
    public static class MethodCallModel implements RValueModel {
        /**
         * Object which method should be called.
         */
        public final VariableModel instance;

        /**
         * Class which static method should be called.
         */
        public final AbstractClassModel classInstance;

        /**
         * Name of the method to be invoked.
         */
        public String methodName;

        /**
         * Arguments passed to the method.
         */
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

    /**
     * Invokes constructor of the super-class, can be used in a constructor body, see {@link ConstructorModel}.
     */
    public static class SuperCallModel implements InstructionModel {
        /**
         * Parent class model.
         */
        public final AbstractClassModel superClass;

        /**
         * Arguments passed to the parent class constructor.
         */
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

    /**
     * Allocates instance of a given type.
     */
    public static class AllocationModel implements RValueModel {
        /**
         * Defines what type of object should be allocated.
         */
        public TypeModel type;

        /**
         * Arguments passed to the constructor of the type.
         */
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
