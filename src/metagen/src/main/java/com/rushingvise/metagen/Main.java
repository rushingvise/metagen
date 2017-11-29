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

import com.rushingvise.metagen.interpreter.BuilderPatternInterpreter;
import com.rushingvise.metagen.interpreter.GraphInterpreter;
import com.rushingvise.metagen.interpreter.GraphInterpreterException;
import com.rushingvise.metagen.generator.*;
import com.rushingvise.metagen.parser.GraphsModel;
import com.rushingvise.metagen.parser.GraphsParser;
import com.rushingvise.metagen.parser.GraphsParserException;
import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) {
        Option inputOption = Option.builder("i")
                .required(true)
                .desc("Spec input file")
                .longOpt("input")
                .hasArg(true)
                .build();
        Option outputOption = Option.builder("o")
                .required(true)
                .desc("Output directory")
                .longOpt("output")
                .hasArg(true)
                .build();

        Option languageOption = Option.builder("l")
                .required(true)
                .desc("Output language [java, cpp]")
                .longOpt("language")
                .hasArg(true)
                .build();

        Option javaPackageOption = Option.builder("jp")
                .required(false)
                .desc("Java package name")
                .longOpt("java-package")
                .hasArg(true)
                .build();

        Option cppNamespaceOption = Option.builder("cn")
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
            GraphsParser graphsParser = new GraphsParser(commandLine.getOptionValue("i"));
            GraphsModel graphsModel = graphsParser.parse();
            GraphInterpreter analyzer = new BuilderPatternInterpreter(graphsModel); // TODO: add analyzer switch
            CodeModel codeModel = analyzer.analyze();
            final String targetLanguage = commandLine.getOptionValue("l");
            final String outputDirectory = commandLine.getOptionValue("o");
            CodeGenerator codeGenerator;
            if ("java".equals(targetLanguage)) {
                final String packageName = commandLine.getOptionValue("jp");
                codeGenerator = new JavaCodeGenerator(outputDirectory, codeModel, packageName);
            } else if ("cpp".equals(targetLanguage)) {
                final String namespaceName = commandLine.getOptionValue("cn");
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
