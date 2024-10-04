package com.glamik.webpconverter.command;

public interface VoidCommand<A> {
    void execute(A argument);
}
