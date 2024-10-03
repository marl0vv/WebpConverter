package com.glamik.webpconverter.command;

public interface Command<A, R> {
    R execute(A argument);
}
