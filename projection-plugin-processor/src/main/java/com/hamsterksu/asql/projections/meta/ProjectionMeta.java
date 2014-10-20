package com.hamsterksu.asql.projections.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hamsterksu on 19.10.2014.
 */
public class ProjectionMeta {

    private final String name;

    private final List<ColumnMeta> columns = new ArrayList<ColumnMeta>();

    public ProjectionMeta(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<ColumnMeta> getColumns() {
        return columns;
    }

    public void addColumn(ColumnMeta c) {
        if (c == null)
            return;
        columns.add(c);
    }

}
