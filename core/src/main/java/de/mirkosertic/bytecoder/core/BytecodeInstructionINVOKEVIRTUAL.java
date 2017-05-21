/*
 * Copyright 2017 Mirko Sertic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mirkosertic.bytecoder.core;

import de.mirkosertic.bytecoder.classlib.java.lang.TArray;

public class BytecodeInstructionINVOKEVIRTUAL extends BytecodeInstruction implements BytecodeInstructionInvoke {

    private final int index;
    private final BytecodeConstantPool constantPool;

    public BytecodeInstructionINVOKEVIRTUAL(BytecodeOpcodeAddress aOpcodeIndex, int aIndex, BytecodeConstantPool aConstantPool) {
        super(aOpcodeIndex);
        index = aIndex;
        constantPool = aConstantPool;
    }

    public BytecodeMethodRefConstant getMethodDescriptor() {
        return (BytecodeMethodRefConstant) constantPool.constantByIndex(index - 1);
    }

    @Override
    public void performLinking(BytecodeLinkerContext aLinkerContext) {
        BytecodeMethodRefConstant theMethodRefConstant = getMethodDescriptor();
        BytecodeClassinfoConstant theClassConstant = theMethodRefConstant.getClassIndex().getClassConstant();
        BytecodeNameAndTypeConstant theMethodRef = theMethodRefConstant.getNameAndTypeIndex().getNameAndType();

        BytecodeMethodSignature theSig = theMethodRef.getDescriptorIndex().methodSignature();
        BytecodeUtf8Constant theName = theMethodRef.getNameIndex().getName();

        BytecodeUtf8Constant theConstant = theClassConstant.getConstant();
        String theClassName = theConstant.stringValue();
        if (theClassName.startsWith("[")) {

            BytecodeTypeRef[] theTypes = aLinkerContext.getSignatureParser().toTypes(theClassName);
            BytecodeTypeRef theSingleType = theTypes[0];
            aLinkerContext.linkTypeRef(theSingleType);

            // We are linking an Array here, so mark the corresponding name here
            BytecodeObjectTypeRef theTypeRef = BytecodeObjectTypeRef.fromRuntimeClass(TArray.class);
            aLinkerContext.linkVirtualMethod(theTypeRef, theName.stringValue(), theSig);

        } else {
            aLinkerContext.linkVirtualMethod(new BytecodeObjectTypeRef(theClassName.replace("/", ".")),
                    theName.stringValue(), theSig);
        }
    }
}