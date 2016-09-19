package com.ailidani.benchmark;

public enum OperationType {

    /**
     * Read
     */
    GET,

    /**
     * Write, returns old value
     */
    PUT,

    /**
     * Write
     */
    SET,

    /**
     * Write, returns old value
     */
    REMOVE,

    /**
     * Write
     */
    DELETE,

    /**
     * Read, currently no return
     */
    SNAPSHOT
}
