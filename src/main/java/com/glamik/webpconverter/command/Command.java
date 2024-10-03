package com.glamik.webpconverter.command;

public interface Command <ARGUMENT, RETURN>{
    RETURN execute(ARGUMENT argument);
}
