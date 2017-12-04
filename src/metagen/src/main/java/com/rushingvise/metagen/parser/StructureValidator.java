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

package com.rushingvise.metagen.parser;

import com.rushingvise.metagen.parser.StructureModel.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.rushingvise.metagen.parser.StructureModel.Utils.findNamedItem;

/**
 * Validates provided {@link StructureModel}.
 */
public class StructureValidator {
    private static final Set<String> BUILT_IN_TYPES = new HashSet<>();
    static {
        BUILT_IN_TYPES.add("string");
        BUILT_IN_TYPES.add("integer");
    }

    private final StructureModel mStructureModel;

    /**
     * @param structureModel {@link GraphModel} which should be validated.
     */
    public StructureValidator(StructureModel structureModel) {
        mStructureModel = structureModel;
    }

    /**
     * Performs validation of the provided {@link GraphModel}.
     * @throws StructureParserException
     */
    public void validate() throws StructureParserException {
        mGraphsModelValidator.validate(null, mStructureModel);
    }

    interface Validator<TContext, T> {
        void validate(TContext context, T object) throws StructureParserException;
    }

    private final Validator<GraphModel, IncludeActionModel> mIncludeActionModelValidator = (context, object) -> {
        validateIfNotNull(findNamedItem(context.actions, object.name), "<include-action> \"name\" attribute points non-existing action \"" + object.getName() + "\".");
    };

    private final Validator<GraphModel, IncludeEdgeModel> mIncludeEdgeModelValidator = (context, object) -> {
        validateIfNotNull(findNamedItem(context.edges, object.name), "<include-edge> \"name\" attribute points non-existing action \"" + object.getName() + "\".");
    };

    private final Validator<GraphModel, NodeModel> mNodeModelValidator = (context, object) -> {
        if (object.includedActions != null) {
            for (IncludeActionModel includeActionModel : object.includedActions) {
                mIncludeActionModelValidator.validate(context, includeActionModel);
            }
        }
        if (object.includedEdges != null) {
            for (IncludeEdgeModel includeEdgeModel : object.includedEdges) {
                mIncludeEdgeModelValidator.validate(context, includeEdgeModel);
            }
        }
    };

    private final Validator<GraphModel, List<ArgumentModel>> mArgumentsValidator = (context, object) -> {
        if (object != null) {
            validateNames(object, "<arg>");
            for (ArgumentModel argumentModel : object) {
                validate(isProperType(argumentModel.type), "<arg name=\"" + argumentModel.name + "\"> type \"" + argumentModel.type + "\" is not known.");
            }
        }
    };

    private final Validator<GraphModel, SignatureModel> mActionSignatureValidator = (context, object) -> {
        mArgumentsValidator.validate(context, object.arguments);
        if (object.returnType != null) {
            validateIfNotEmpty(object.returnType, "<signature name=\"" + object.name + "\"> return attribute cannot be empty.");
            validate(isProperType(object.returnType), "<signature name=\"" + object.name + "\"> type \"" + object.returnType + "\" in return attribute is not known.");
        }
    };

    private final Validator<GraphModel, SignatureModel> mEdgeSignatureValidator = (context, object) -> {
        mArgumentsValidator.validate(context, object.arguments);
        validate(object.returnType == null, "<signature name=\"" + object.name + "\"> return attribute not allowed.");
    };

    private boolean isProperType(String typeName) {
        if (BUILT_IN_TYPES.contains(typeName)) {
            return true;
        } else {
            return findNamedItem(mStructureModel.types, typeName) != null;
        }
    }

    private final Validator<GraphModel, ActionModel> mActionModelValidator = (context, object) -> {
        validateNames(object.signatures, "<signature>");
        for (SignatureModel signatureModel : object.signatures) {
            mActionSignatureValidator.validate(context, signatureModel);
        }
    };

    private final Validator<GraphModel, EdgeModel> mEdgeModelValidator = (context, object) -> {
        validateNames(object.signatures, "<signature>");
        validateIfNotNull(findNamedItem(context.nodes, object.target), "<edge> \"target\" attribute points non-existing node \"" + object.target + "\".");
        for (SignatureModel signatureModel : object.signatures) {
            mEdgeSignatureValidator.validate(context, signatureModel);
        }
    };

    private final Validator<StructureModel, GraphModel> mGraphModelValidator = (context, object) -> {
        validateNames(object.nodes, "<node>");
        for (NodeModel nodeModel : object.nodes) {
            mNodeModelValidator.validate(object, nodeModel);
        }
        validateNames(object.actions, "<action>");
        for (ActionModel actionModel : object.actions) {
            mActionModelValidator.validate(object, actionModel);
        }
        validateNames(object.edges, "<edge>");
        for (EdgeModel edgeModel : object.edges) {
            mEdgeModelValidator.validate(object, edgeModel);
        }
        validateIfNotEmpty(object.initialNode, "<graph> \"initial_node\" attribute is required.");
        validateIfNotNull(findNamedItem(object.nodes, object.initialNode), "<graph> \"initial_node\" attribute points non-existing node \"" + object.initialNode + "\".");
    };

    private final Validator<Void, StructureModel> mGraphsModelValidator = (context, object) -> {
        validateNames(object.graphs, "<graph>");
        validateNames(object.types, "<type>");
        for (GraphModel graphModel : object.graphs) {
            mGraphModelValidator.validate(object, graphModel);
        }
    };

    private static void validateNames(List<? extends NamedModel> namedModels, String tag) throws StructureParserException {
        Set<String> names = new HashSet<>();
        for (NamedModel model : namedModels) {
            validateIfNotEmpty(model.getName(), tag + " \"name\" attribute is required.");
            if (names.contains(model.getName())) {
                throw new StructureParserException(tag + " \"name\" attribute with value \"" + model.getName() + "\" is duplicated.");
            } else {
                names.add(model.getName());
            }
        }
    }

    private static void validateIfNotEmpty(String value, String message) throws StructureParserException {
        if (value == null || value.trim().length() == 0) {
            throw new StructureParserException(message);
        }
    }

    private static void validateIfNotNull(Object value, String message) throws StructureParserException {
        if (value == null) {
            throw new StructureParserException(message);
        }
    }

    private static void validate(boolean condition, String message) throws StructureParserException {
        if (!condition) {
            throw new StructureParserException(message);
        }
    }
}
