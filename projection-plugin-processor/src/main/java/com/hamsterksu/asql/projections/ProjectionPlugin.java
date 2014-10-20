package com.hamsterksu.asql.projections;

import com.annotatedsql.annotation.provider.Provider;
import com.annotatedsql.annotation.provider.Providers;
import com.annotatedsql.annotation.provider.URI;
import com.annotatedsql.ftl.SchemaMeta;
import com.annotatedsql.ftl.TableColumns;
import com.annotatedsql.ftl.ViewMeta;
import com.annotatedsql.ftl.ViewMeta.ViewTableInfo;
import com.annotatedsql.processor.ProcessorLogger;
import com.annotatedsql.processor.logger.TagLogger;
import com.annotatedsql.processor.sql.TableResult;
import com.annotatedsql.util.ClassUtil;
import com.annotatedsql.util.TextUtils;
import com.hamsterksu.asql.projections.meta.ColumnMeta;
import com.hamsterksu.asql.projections.meta.ProjectionMeta;
import com.hamsterksu.asql.projections.meta.SchemaProjectionsMeta;
import com.hamsterksu.asql.projections.meta.TableProjectionsMeta;
import com.hamsterksu.asql.projections.meta.UriMeta;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Created by hamsterksu on 19.10.2014.
 */
public class ProjectionPlugin implements com.annotatedsql.processor.sql.ISchemaPlugin {

    private static final Pattern CUSTOM_COLUMN_NAME_PATTERN = Pattern.compile("^(.+[\\),\\s])as(\\s.+)");

    private TagLogger logger;
    private ProcessingEnvironment processingEnv;
    private Configuration cfg = new Configuration();
    private Trees trees;

    private SchemaProjectionsMeta schemaProjections;

    @Override
    public void init(ProcessingEnvironment processingEnv, ProcessorLogger logger) {
        this.processingEnv = processingEnv;
        this.logger = new TagLogger("ProjectionPlugin", logger);
        this.trees = Trees.instance(processingEnv);
        cfg.setTemplateLoader(new ClassTemplateLoader(this.getClass(), "/res"));

        schemaProjections = new SchemaProjectionsMeta();
    }

    @Override
    public void processTable(TypeElement element, TableResult tableInfo) {
        List<String> defColumns = new ArrayList<String>();
        TableColumns tableColumns = tableInfo.getTableColumns();
        for (Entry<String, String> e : tableColumns.getColumn2Variable().entrySet()) {
            defColumns.add(e.getValue());
        }
        proceedProjections(element, defColumns);
    }

    @Override
    public void processView(TypeElement element, ViewMeta meta) {
        List<String> defColumns = new ArrayList<String>();
        for (ViewTableInfo t : meta.getTables()) {
            String tableClassName = t.getClassName();
            for (com.annotatedsql.ftl.ColumnMeta c : t.getColumns()) {
                defColumns.add(meta.getViewClassName() + "2." + tableClassName + "." + c.getVariableName());
            }
        }
        proceedProjections(element, defColumns);
    }

    @Override
    public void processRawQuery(TypeElement element, ViewMeta meta) {
        processView(element, meta);
    }

    @Override
    public void processSchema(TypeElement element, SchemaMeta model) {
        Providers providers = element.getAnnotation(Providers.class);
        String providerClass = "";
        if(providers != null){
            Provider[] values = providers.value();
            if(values != null && values.length > 0 && values[0] != null){
                providerClass = values[0].name();
            }
        }else {
            Provider provider = element.getAnnotation(Provider.class);
            if (provider != null) {
                providerClass = provider.name();
            }
        }
        schemaProjections.setPkgName(model.getPkgName());
        schemaProjections.setStoreClassName(model.getStoreClassName());
        schemaProjections.setSchemaClassName(model.getClassName());
        schemaProjections.setProviderClass(providerClass);
        generateProjectionsFile();
    }

    private void generateProjectionsFile() {
        this.logger.i("generateProjectionsFile");
        JavaFileObject file;
        try {
            String className = schemaProjections.getPkgName() + ".Projections";
            file = processingEnv.getFiler().createSourceFile(className);
            logger.i("generateProjectionsFile: Creating file:  " + className + " in " + file.getName());
            Writer out = file.openWriter();
            Template t = cfg.getTemplate("projections.ftl");
            t.process(schemaProjections, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            logger.e("generateProjectionsFile error:", e);
        } catch (TemplateException e) {
            logger.e("generateProjectionsFile error:", e);
        }
        this.logger.i("generateProjectionsFile end");
    }

    private void proceedProjections(TypeElement element, List<String> defaultColumns) {
        String className = element.getSimpleName().toString();
        TableProjectionsMeta tableProjections = new TableProjectionsMeta(className);

        for (Element e : ClassUtil.getAllClassFields(element)) {
            VariableElement var = ((VariableElement) e);
            Object nameValue = var.getConstantValue();
            String varValue = nameValue == null ? null : nameValue.toString();

            URI uri = e.getAnnotation(URI.class);
            if (uri != null && !TextUtils.isEmpty(varValue)) {
                tableProjections.addUri(new UriMeta(e.getSimpleName().toString(), varValue));
            }
            Projection projection = e.getAnnotation(Projection.class);
            if (projection == null) {
                continue;
            }
            if (TextUtils.isEmpty(varValue)) {
                logger.w("projection name is empty");
                continue;
            }

            logger.i("parse projection: var = " + var.getSimpleName() + "; name = " + varValue);
            ProjectionMeta projectionMeta = new ProjectionMeta(varValue);

            CodeAnalyzerTreeScanner codeScanner = new CodeAnalyzerTreeScanner();
            TreePath tp = this.trees.getPath(e);

            List<String> columns = codeScanner.scan(tp, this.trees);
            if (columns.isEmpty() && defaultColumns != null) {
                columns.addAll(defaultColumns);
            }
            addColumns2Projection(className, projectionMeta, columns);
            tableProjections.addProjection(projectionMeta);
        }
        if (!tableProjections.isEmpty()) {
            schemaProjections.addProjection(tableProjections);
        }
    }

    private void addColumns2Projection(String className, ProjectionMeta projectionMeta, List<String> columns) {
        int i = 0;
        for (String c : columns) {
            i++;
            ColumnMeta columnMeta;
            if (c.contains("+") || c.contains(" ")) {
                //parse something like "count(ID) as b" or "count( + ID + ) as b" or "a as b"
                Matcher matcher = CUSTOM_COLUMN_NAME_PATTERN.matcher(c);
                if (matcher.matches()) {
                    String columnName = matcher.group(matcher.groupCount());
                    columnName = columnName.trim();
                    if (columnName.startsWith("\"")) {
                        columnName = columnName.substring(1);
                    }
                    if (columnName.endsWith("\"")) {
                        columnName = columnName.substring(0, columnName.length() - 1);
                    }
                    columnName = columnName.trim();
                    columnMeta = new ColumnMeta(columnName, c);
                } else {
                    columnMeta = new ColumnMeta("UNKNOWN" + i, c);
                }
            } else if (c.contains(".")) {
                //parse something like PostsView2.PostTable.ID or PostTable.ID
                String[] pars = c.split("\\.");
                int offset = 0;
                if (pars.length > 1) {
                    offset = 1;
                }
                StringBuilder builder = new StringBuilder(c.length());
                for (int j = offset; j < pars.length; j++) {
                    builder.append(pars[j]).append("_");
                }
                if (builder.length() > 0) {
                    builder.setLength(builder.length() - 1);
                }
                String columnName = builder.toString();
                columnMeta = new ColumnMeta(columnName, c);
            } else {
                columnMeta = new ColumnMeta(c, className + "." + c);
            }
            projectionMeta.addColumn(columnMeta);
        }
    }

    private static class CodeAnalyzerTreeScanner extends TreePathScanner<List<String>, Trees> {

        @Override
        public List<String> visitVariable(VariableTree variableTree, Trees trees) {
            List<String> columns = new ArrayList<String>();
            List<? extends AnnotationTree> annotationTrees = variableTree.getModifiers().getAnnotations();
            for (AnnotationTree a : annotationTrees) {
                for (ExpressionTree e : a.getArguments()) {
                    AssignmentTree assign = (AssignmentTree) e;
                    ExpressionTree value = assign.getExpression();
                    if (value instanceof NewArrayTree) {
                        NewArrayTree newArrayTree = (NewArrayTree) assign.getExpression();
                        for (ExpressionTree i : newArrayTree.getInitializers()) {
                            columns.add(i.toString());
                        }
                    } else {
                        columns.add(value.toString());
                    }
                }
            }
            return columns;
        }
    }
}
