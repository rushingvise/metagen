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

import com.rushingvise.metagen.parser.GraphsModel.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.rushingvise.metagen.parser.GraphsModel.Utils.findNamedItem;

public class GraphsValidator {
    private final GraphsModel mGraphsModel;

    public GraphsValidator(GraphsModel graphsModel) {
        mGraphsModel = graphsModel;
    }

    public void validate() throws GraphsParserException {
        mGraphsModelValidator.validate(null, mGraphsModel);
    }

    interface Validator<TContext, T> {
        void validate(TContext context, T object) throws GraphsParserException;
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

    private final Validator<SignatureModelHolder, SignatureModel> mSignatureValidator = (context, object) -> {
        if (object.arguments != null) {
            validateNames(object.arguments, "<arg>");
        }

        if (!context.isReturnTypeAllowed()) {
            validate(object.returnType == null, "<signature name=\"" + object.name + "\"> return typeName not allowed.");
        } else if (object.returnType != null) {
            validateIfNotEmpty(object.returnType, "<signature name=\"" + object.name + "\"> return typeName cannot be empty.");
        }
    };

    private final Validator<GraphModel, ActionModel> mActionModelValidator = (context, object) -> {
        validateNames(object.signatures, "<signature>");
        for (SignatureModel signatureModel : object.signatures) {
            mSignatureValidator.validate(object, signatureModel);
        }
    };

    private final Validator<GraphModel, EdgeModel> mEdgeModelValidator = (context, object) -> {
        validateNames(object.signatures, "<signature>");
        validateIfNotNull(findNamedItem(context.nodes, object.target), "<edge> \"target\" attribute points non-existing node \"" + object.target + "\".");
        for (SignatureModel signatureModel : object.signatures) {
            mSignatureValidator.validate(object, signatureModel);
        }
    };

    private final Validator<GraphsModel, GraphModel> mGraphModelValidator = (context, object) -> {
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

    private final Validator<Void, GraphsModel> mGraphsModelValidator = (context, object) -> {
        validateNames(object.graphs, "<graph>");
        for (GraphModel graphModel : object.graphs) {
            mGraphModelValidator.validate(object, graphModel);
        }
    };

    private static void validateNames(List<? extends NamedModel> namedModels, String tag) throws GraphsParserException {
        Set<String> names = new HashSet<>();
        for (NamedModel model : namedModels) {
            validateIfNotEmpty(model.getName(), tag + " \"name\" attribute is required.");
            if (names.contains(model.getName())) {
                throw new GraphsParserException(tag + " \"name\" attribute with value \"" + model.getName() + "\" is duplicated.");
            } else {
                names.add(model.getName());
            }
        }
    }

    private static void validateIfNotEmpty(String value, String message) throws GraphsParserException {
        if (value == null || value.trim().length() == 0) {
            throw new GraphsParserException(message);
        }
    }

    private static void validateIfNotNull(Object value, String message) throws GraphsParserException {
        if (value == null) {
            throw new GraphsParserException(message);
        }
    }

    private static void validate(boolean condition, String message) throws GraphsParserException {
        if (!condition) {
            throw new GraphsParserException(message);
        }
    }
}
