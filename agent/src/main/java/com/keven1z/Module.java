package com.keven1z;


import java.lang.instrument.Instrumentation;

public interface Module {
    void start(Instrumentation inst) throws Throwable;
}
