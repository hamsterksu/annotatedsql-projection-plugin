package com.hamsterksu.asql.projections.meta;

/**
 * Created by hamsterksu on 19.10.2014.
 */
public class ColumnMeta {

    private final String name;

    private final String expr;

    public ColumnMeta(String name, String expr) {
        this.name = name.toUpperCase();
        this.expr = expr;
    }

    public String getName() {
        return name;
    }

    public String getExpr() {
        return expr;
    }
}
