package com.hamsterksu.asql.projections.meta;

/**
 * Created by hamsterksu on 20.10.2014.
 */
public class UriMeta {

    private final String varName;
    private final String path;

    public UriMeta(String varName, String path) {
        this.varName = varName;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getVarName() {
        return varName;
    }
}
