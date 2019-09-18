/*
 * Copyright (c) 2017, 2019, Oracle and/or its affiliates.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.truffle.llvm.parser.factories;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.oracle.truffle.llvm.runtime.LLVMSyscallEntry;
import com.oracle.truffle.llvm.runtime.PlatformCapability;
import com.oracle.truffle.llvm.runtime.memory.LLVMSyscallOperationNode;
import com.oracle.truffle.llvm.runtime.nodes.asm.syscall.LLVMAMD64UnknownSyscallNode;
import com.oracle.truffle.llvm.runtime.nodes.asm.syscall.LLVMInfo;

public abstract class BasicPlatformCapability<S extends Enum<S> & LLVMSyscallEntry> extends PlatformCapability<S> {

    public static BasicPlatformCapability<?> create(boolean loadCxxLibraries) {
        if (LLVMInfo.SYSNAME.toLowerCase().equals("linux") && LLVMInfo.MACHINE.toLowerCase().equals("x86_64")) {
            return new LinuxAMD64PlatformCapability(loadCxxLibraries);
        }
        if (LLVMInfo.SYSNAME.toLowerCase().equals("mac os x") && LLVMInfo.MACHINE.toLowerCase().equals("x86_64")) {
            return new DarwinAMD64PlatformCapability(loadCxxLibraries);
        }
        return new UnknownBasicPlatformCapability(loadCxxLibraries);
    }

    private static final Path SULONG_LIBDIR = Paths.get("native", "lib");
    public static final String LIBSULONG_FILENAME = "libsulong.bc";
    public static final String LIBSULONGXX_FILENAME = "libsulong++.bc";

    private final boolean loadCxxLibraries;

    protected BasicPlatformCapability(Class<S> cls, boolean loadCxxLibraries) {
        super(cls);
        this.loadCxxLibraries = loadCxxLibraries;
    }

    @Override
    public Path getSulongLibrariesPath() {
        return SULONG_LIBDIR;
    }

    @Override
    public String[] getSulongDefaultLibraries() {
        if (loadCxxLibraries) {
            return new String[]{LIBSULONG_FILENAME, LIBSULONGXX_FILENAME};
        } else {
            return new String[]{LIBSULONG_FILENAME};
        }
    }

    @Override
    public LLVMSyscallOperationNode createSyscallNode(long index) {
        try {
            return createSyscallNode(getSyscall(index));
        } catch (IllegalArgumentException e) {
            return new LLVMAMD64UnknownSyscallNode(index);
        }
    }

    protected abstract LLVMSyscallOperationNode createSyscallNode(S syscall);
}
