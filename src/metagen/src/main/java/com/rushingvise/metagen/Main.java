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

package com.rushingvise.metagen;

import com.rushingvise.metagen.generator.*;
import com.rushingvise.metagen.interpreter.BuilderPatternInterpreter;
import com.rushingvise.metagen.interpreter.GraphInterpreter;
import com.rushingvise.metagen.interpreter.GraphInterpreterException;
import com.rushingvise.metagen.parser.GraphsModel;
import com.rushingvise.metagen.parser.GraphsParser;
import com.rushingvise.metagen.parser.GraphsParserException;
import org.apache.commons.cli.*;

public class Main {
    private static final String LANGUAGE_JAVA = "java";
    private static final String LANGUAGE_CPP = "cpp";

    public static void main(String[] args) {
        final Option inputOption = Option.builder("i")
                .required(true)
                .desc("Spec input file")
                .longOpt("input")
                .hasArg(true)
                .build();
        final Option outputOption = Option.builder("o")
                .required(true)
                .desc("Output directory")
                .longOpt("output")
                .hasArg(true)
                .build();

        final Option languageOption = Option.builder("l")
                .required(true)
                .desc("Output language [java, cpp]")
                .longOpt("language")
                .hasArg(true)
                .build();

        final Option javaPackageOption = Option.builder("jp")
                .required(false)
                .desc("Java package name")
                .longOpt("java-package")
                .hasArg(true)
                .build();

        final Option cppNamespaceOption = Option.builder("cn")
                .required(false)
                .desc("Java package name")
                .longOpt("cpp-namespace")
                .hasArg(true)
                .build();

        Options options = new Options();
        CommandLineParser parser = new DefaultParser();

        options.addOption(inputOption);
        options.addOption(outputOption);
        options.addOption(languageOption);
        options.addOption(javaPackageOption);
        options.addOption(cppNamespaceOption);

        try {
            CommandLine commandLine = parser.parse(options, args, false);

            // Parsing the graph specification
            GraphsParser graphsParser = new GraphsParser(commandLine.getOptionValue(inputOption.getOpt()));
            GraphsModel graphsModel = graphsParser.parse();

            // Interpreting the graph model and creating code model based on it
            GraphInterpreter analyzer = new BuilderPatternInterpreter(graphsModel); // TODO: add analyzer switch
            CodeModel codeModel = analyzer.analyze();

            // Generating final code
            final String targetLanguage = commandLine.getOptionValue(languageOption.getOpt());
            final String outputDirectory = commandLine.getOptionValue(outputOption.getOpt());
            CodeGenerator codeGenerator;
            if (LANGUAGE_JAVA.equals(targetLanguage)) {
                final String packageName = commandLine.getOptionValue(javaPackageOption.getOpt());
                codeGenerator = new JavaCodeGenerator(outputDirectory, codeModel, packageName);
            } else if (LANGUAGE_CPP.equals(targetLanguage)) {
                final String namespaceName = commandLine.getOptionValue(cppNamespaceOption.getOpt());
                codeGenerator = new CppCodeGenerator(outputDirectory, codeModel, namespaceName);
            } else {
                throw new ParseException("Unsupported language: " + targetLanguage);
            }
            codeGenerator.generate();
        } catch (ParseException exception) {
            System.out.println(exception.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("metagen", options);
        } catch (GraphsParserException e) {
            System.out.println("Exception occurred while parsing the specification: " + e.getMessage());
        } catch (CodeGeneratorException e) {
            System.out.println("Exception occurred while generating the code: " + e.getMessage());
        } catch (GraphInterpreterException e) {
            System.out.println("Exception occurred while compiling the model: " + e.getMessage());
        }
    }
}
