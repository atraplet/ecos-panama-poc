// Generated by jextract

package com.ustermetrics.ecos.stubs;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.*;
class constants$0 {

    static final FunctionDescriptor ECOS_setup$FUNC = FunctionDescriptor.of(C_POINTER,
        C_LONG,
        C_LONG,
        C_LONG,
        C_LONG,
        C_LONG,
        C_POINTER,
        C_LONG,
        C_POINTER,
        C_POINTER,
        C_POINTER,
        C_POINTER,
        C_POINTER,
        C_POINTER,
        C_POINTER,
        C_POINTER,
        C_POINTER
    );
    static final MethodHandle ECOS_setup$MH = RuntimeHelper.downcallHandle(
        ecos_h.LIBRARIES, "ECOS_setup",
        "(JJJJJLjdk/incubator/foreign/MemoryAddress;JLjdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;Ljdk/incubator/foreign/MemoryAddress;)Ljdk/incubator/foreign/MemoryAddress;",
        constants$0.ECOS_setup$FUNC, false
    );
    static final FunctionDescriptor ECOS_solve$FUNC = FunctionDescriptor.of(C_LONG,
        C_POINTER
    );
    static final MethodHandle ECOS_solve$MH = RuntimeHelper.downcallHandle(
        ecos_h.LIBRARIES, "ECOS_solve",
        "(Ljdk/incubator/foreign/MemoryAddress;)J",
        constants$0.ECOS_solve$FUNC, false
    );
    static final FunctionDescriptor ECOS_cleanup$FUNC = FunctionDescriptor.ofVoid(
        C_POINTER,
        C_LONG
    );
    static final MethodHandle ECOS_cleanup$MH = RuntimeHelper.downcallHandle(
        ecos_h.LIBRARIES, "ECOS_cleanup",
        "(Ljdk/incubator/foreign/MemoryAddress;J)V",
        constants$0.ECOS_cleanup$FUNC, false
    );
    static final FunctionDescriptor ECOS_ver$FUNC = FunctionDescriptor.of(C_POINTER);
    static final MethodHandle ECOS_ver$MH = RuntimeHelper.downcallHandle(
        ecos_h.LIBRARIES, "ECOS_ver",
        "()Ljdk/incubator/foreign/MemoryAddress;",
        constants$0.ECOS_ver$FUNC, false
    );
}


