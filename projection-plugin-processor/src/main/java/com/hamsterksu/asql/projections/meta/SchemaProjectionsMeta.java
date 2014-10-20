package com.hamsterksu.asql.projections.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hamsterksu on 19.10.2014.
 */
public class SchemaProjectionsMeta {

    private String pkgName;
    private String storeClassName;
    private String schemaClassName;
    private String providerClass;

    private final List<TableProjectionsMeta> projections = new ArrayList<TableProjectionsMeta>();

    public void addProjection(TableProjectionsMeta p) {
        projections.add(p);
    }

    public List<TableProjectionsMeta> getProjections() {
        return projections;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getStoreClassName() {
        return storeClassName;
    }

    public void setStoreClassName(String storeClassName) {
        this.storeClassName = storeClassName;
    }

    public String getSchemaClassName() {
        return schemaClassName;
    }

    public void setSchemaClassName(String schemaClassName) {
        this.schemaClassName = schemaClassName;
    }

    public String getProviderClass() {
        return providerClass;
    }

    public void setProviderClass(String providerClass) {
        this.providerClass = providerClass;
    }
}
