package com.hamsterksu.asql.projections.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hamsterksu on 19.10.2014.
 */
public class TableProjectionsMeta {

    private final String tableName;

    private final List<UriMeta> uris = new ArrayList<UriMeta>();

    private final List<ProjectionMeta> projections = new ArrayList<ProjectionMeta>();

    public TableProjectionsMeta(String tableName) {
        this.tableName = tableName;
    }

    public void addProjection(ProjectionMeta p) {
        projections.add(p);
    }

    public List<ProjectionMeta> getProjections() {
        return projections;
    }

    public boolean isEmpty() {
        return projections.isEmpty();
    }

    public String getTableName() {
        return tableName;
    }


    public void addUri(UriMeta uri){
        if(uri == null)
            return;
        uris.add(uri);
    }

    public List<UriMeta> getUris() {
        return uris;
    }
}
