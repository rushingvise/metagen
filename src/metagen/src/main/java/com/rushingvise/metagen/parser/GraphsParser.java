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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * XML-based graphs specification parser.
 */
public class GraphsParser {
    private final String mFilePath;

    /**
     * @param filePath Path to the specification file.
     */
    public GraphsParser(String filePath) {
        mFilePath = filePath;
    }

    /**
     * Parser the provided specification file.
     * @return GraphsModel instance.
     * @throws GraphsParserException
     */
    public GraphsModel parse() throws GraphsParserException {
        File inputFile = new File(mFilePath);
        GraphsModel graphsModel;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(GraphsModel.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            graphsModel = (GraphsModel) jaxbUnmarshaller.unmarshal(inputFile);
            GraphsValidator validator = new GraphsValidator(graphsModel);
            validator.validate();
        } catch (JAXBException e) {
            throw new GraphsParserException(e);
        }

        return graphsModel;
    }
}
