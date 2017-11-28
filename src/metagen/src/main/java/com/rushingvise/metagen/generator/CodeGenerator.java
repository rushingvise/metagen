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

public abstract class CodeGenerator {
    protected final String mOutputPath;
    protected final CodeModel mCodeModel;

    public CodeGenerator(String outputPath, CodeModel codeModel) {
        mOutputPath = outputPath;
        mCodeModel = codeModel;
    }

    public abstract void generate() throws CodeGeneratorException;

    protected static class CodePrintWriter {
        private PrintWriter mWriter;
        private StringBuilder mIndentation = new StringBuilder();
        private final String mSingleIndent;
        private final int INDENT_WIDTH = 4;

        public CodePrintWriter(OutputStream out) {
            mWriter = new PrintWriter(out, true);
            for (int i = 0; i < INDENT_WIDTH; ++i) {
                mIndentation.append(' ');
            }
            mSingleIndent = mIndentation.toString();
            mIndentation.setLength(0);
        }

        public void print(String line) {
            StringTokenizer tokenizer = new StringTokenizer(line, "\n");
            while (tokenizer.hasMoreElements()) {
                mWriter.print(mIndentation.toString() + tokenizer.nextElement());
                if (tokenizer.hasMoreElements()) {
                    mWriter.println();
                }
            }
        }

        public void println(String line) {
            StringTokenizer tokenizer = new StringTokenizer(line, "\n");
            while (tokenizer.hasMoreElements()) {
                mWriter.print(mIndentation.toString() + tokenizer.nextElement());
                mWriter.println();
            }
        }

        public void println() {
            mWriter.println();
        }

        public void block(String line, CodeBlock block) throws CodeGeneratorException {
            block(line, block, "");
        }

        public void block(String line, CodeBlock block, String blockSuffix) throws CodeGeneratorException {
            println(line + " {");
            mIndentation.append(mSingleIndent);
            block.writeBlock();
            mIndentation.setLength(mIndentation.length() - INDENT_WIDTH);
            mWriter.println(mIndentation.toString() + "}" + blockSuffix);
        }

        public interface CodeBlock {
            void writeBlock() throws CodeGeneratorException;
        }
    }

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
