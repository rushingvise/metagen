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

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "graphs")
public class GraphsModel {
    @XmlElements(
            @XmlElement(name = "graph", type = GraphModel.class)
    )
    public List<GraphModel> graphs;

    public interface NamedModel {
        String getName();
    }

    public interface SignatureModelHolder {
        boolean isReturnTypeAllowed();
    }

    @XmlRootElement(name = "graph")
    public static class GraphModel implements NamedModel {
        @XmlAttribute(name = "name", required = true)
        public String name;

        @XmlAttribute(name = "initial_node", required = true)
        public String initialNode;

        @XmlElementWrapper(name = "edges", required = true)
        @XmlElement(name = "edge")
        public List<EdgeModel> edges;

        @XmlElementWrapper(name = "actions", required = true)
        @XmlElement(name = "action")
        public List<ActionModel> actions;

        @XmlElementWrapper(name = "nodes", required = true)
        @XmlElement(name = "node")
        public List<NodeModel> nodes;

        @Override
        public String getName() {
            return name;
        }
    }

    @XmlRootElement(name = "edge")
    public static class EdgeModel implements NamedModel, SignatureModelHolder {
        @XmlAttribute(name = "name", required = true)
        public String name;


        @XmlAttribute(name = "target", required = true)
        public String target;

        @XmlElements(
                @XmlElement(name = "signature", type = SignatureModel.class)
        )
        public List<SignatureModel> signatures;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isReturnTypeAllowed() {
            return false;
        }
    }

    @XmlRootElement(name = "action")
    public static class ActionModel implements NamedModel, SignatureModelHolder {
        @XmlAttribute(name = "name", required = true)
        public String name;

        @XmlElements(
                @XmlElement(name = "signature", type = SignatureModel.class)
        )
        public List<SignatureModel> signatures;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isReturnTypeAllowed() {
            return true;
        }
    }

    @XmlRootElement(name = "node")
    public static class NodeModel implements NamedModel {
        @XmlAttribute(name = "name", required = true)
        public String name;

        @XmlElements(
                @XmlElement(name = "include-edge", type = IncludeEdgeModel.class)
        )
        public List<IncludeEdgeModel> includedEdges;

        @XmlElements(
                @XmlElement(name = "include-action", type = IncludeActionModel.class)
        )
        public List<IncludeActionModel> includedActions;

        @Override
        public String getName() {
            return name;
        }
    }

    @XmlRootElement(name = "include-edge")
    public static class IncludeEdgeModel implements NamedModel {
        @XmlAttribute(name = "name", required = true)
        public String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @XmlRootElement(name = "include-transformation")
    public static class IncludeActionModel implements NamedModel {
        @XmlAttribute(name = "name", required = true)
        public String name;

        @Override
        public String getName() {
            return name;
        }
    }

    @XmlRootElement(name = "signature")
    public static class SignatureModel implements NamedModel {
        @XmlAttribute(name = "name", required = true)
        public String name;

        @XmlElements(
                @XmlElement(name = "arg", type = ArgumentModel.class)
        )
        public List<ArgumentModel> arguments;

        @XmlAttribute(name = "return")
        public String returnType;

        @Override
        public String getName() {
            return name;
        }
    }

    @XmlRootElement(name = "arg")
    public static class ArgumentModel implements NamedModel {
        @XmlAttribute(name = "name", required = true)
        public String name;

        @XmlAttribute(name = "typeName", required = true)
        public String type;

        @XmlAttribute(name = "vararg")
        public boolean vararg;

        @XmlAttribute(name = "array")
        public boolean array;

        @Override
        public String getName() {
            return name;
        }
    }

    public static abstract class Utils {
        public static <T extends NamedModel> T findNamedItem(List<T> items, String name) {
            for (T model : items) {
                if (name.equals(model.getName())) {
                    return model;
                }
            }
            return null;
        }
    }
}
