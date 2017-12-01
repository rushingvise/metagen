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

import com.rushingvise.metagen.generator.CodeModel.*;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 * Base class for all code generators.
 */
public abstract class CodeGenerator {
    protected final String mOutputPath;
    protected final CodeModel mCodeModel;

    /**
     * @param outputPath Directory to which the code should be generated.
     * @param codeModel Language-agnostic description of the code to be generated.
     */
    public CodeGenerator(String outputPath, CodeModel codeModel) {
        mOutputPath = outputPath;
        mCodeModel = codeModel;
    }

    /**
     * Main function responsible for generating the code.
     * Should be implemented in the child classes.
     * @throws CodeGeneratorException
     */
    public abstract void generate() throws CodeGeneratorException;

    /**
     * Utility class, which simplifies writing code structures.
     */
    protected static class CodePrintWriter {
        private PrintWriter mWriter;
        private StringBuilder mIndentation = new StringBuilder();
        private final String mSingleIndent;
        private final int INDENT_WIDTH = 4;

        /**
         * @param out Output stream which should be wrapped by this class.
         */
        public CodePrintWriter(OutputStream out) {
            mWriter = new PrintWriter(out, true);
            for (int i = 0; i < INDENT_WIDTH; ++i) {
                mIndentation.append(' ');
            }
            mSingleIndent = mIndentation.toString();
            mIndentation.setLength(0);
        }

        /**
         * Prints out the line to the wrapped output stream, without a newline character.
         * @param line Text to be written.
         */
        public void print(String line) {
            StringTokenizer tokenizer = new StringTokenizer(line, "\n");
            while (tokenizer.hasMoreElements()) {
                mWriter.print(mIndentation.toString() + tokenizer.nextElement());
                if (tokenizer.hasMoreElements()) {
                    mWriter.println();
                }
            }
        }

        /**
         * Prints out the line to the wrapped output stream with a newline character.
         * @param line Text to be written.
         */
        public void println(String line) {
            StringTokenizer tokenizer = new StringTokenizer(line, "\n");
            while (tokenizer.hasMoreElements()) {
                mWriter.print(mIndentation.toString() + tokenizer.nextElement());
                mWriter.println();
            }
        }

        /**
         * Prints out a newline character to the wrapped output stream.
         */
        public void println() {
            mWriter.println();
        }

        /**
         * Writes an indented block of code wrapped in curly brackets.
         * {@code line} is written before opening curly bracket.
         * @param line Line to be written before the opening curly bracket.
         * @param block Block of the code to be written.
         * @throws CodeGeneratorException
         */
        public void block(String line, CodeBlock block) throws CodeGeneratorException {
            block(line, block, "");
        }

        /**
         * Opens a block of code by writing line and opening curly bracket and by increasing indentation.
         * @param line Line to be written before the opening curly bracket.
         */
        public void openBlock(String line) {
            println(line + " {");
            mIndentation.append(mSingleIndent);
        }

        /**
         * Closes the block of code by writing closing curly bracket and {@code blockSuffix} right after it, also decreases the indentation.
         * @param blockSuffix Text to be written after the closing curly bracket.
         */
        public void closeBlock(String blockSuffix) {
            mIndentation.setLength(mIndentation.length() - INDENT_WIDTH);
            mWriter.println(mIndentation.toString() + "}" + blockSuffix);
        }

        /**
         * Writes an indented block of code wrapped in curly brackets.
         * {@code line} is written before opening curly bracket and {@code blockSuffix} right after the closing curly bracket.
         * @param line Line to be written before the opening curly bracket.
         * @param block Block of the code to be written.
         * @param blockSuffix Text to be written after the closing curly bracket.
         * @throws CodeGeneratorException
         */
        public void block(String line, CodeBlock block, String blockSuffix) throws CodeGeneratorException {
            openBlock(line);
            block.writeBlock();
            closeBlock(blockSuffix);
        }

        /**
         * Wrapper for the code block.
         */
        public interface CodeBlock {
            void writeBlock() throws CodeGeneratorException;
        }
    }

    /**
     * Visitor pattern that is used for generating method bodies.
     */
    public interface InstructionModelSerializer {
        String visit(StringValueModel stringValueModel);

        String visit(IntegerValueModel integerValueModel);

        String visit(NullValueModel nullValueModel);

        String visit(VariableModel variableModel);

        String visit(DeclarationModel declarationModel);

        String visit(AssignmentModel assignmentModel);

        String visit(ReturnInstructionModel returnInstructionModel);

        String visit(MethodCallModel methodCallModel);

        String visit(AllocationModel allocationModel);

        String visit(SuperCallModel superCallModel);
    }
}
