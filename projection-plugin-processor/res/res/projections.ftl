/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the AnnotatedSQL library.
 */
package ${pkgName};

import ${pkgName}.${storeClassName}.*;
import ${pkgName}.${schemaClassName}2.*;
import android.net.Uri;

public final class Projections {

    private Projections(){}
    <#list projections as t>

    public static class ${t.tableName}Query{
        <#list t.uris as uri>

        public final static Uri ${uri.varName} = ${providerClass}.contentUri(${t.tableName}.${uri.varName});
        public final static Uri ${uri.varName}_NO_NOTIFY = ${providerClass}.contentUriNoNotify(${t.tableName}.${uri.varName});
        </#list>
        <#list t.projections as p>

        public static class ${p.name}{

            public static final String[] PROJECTION = new String[]{
                <#list p.columns as c>
                ${c.expr}<#if c_has_next>,</#if>
                </#list>
            };

            <#list p.columns as c>
            public static final int INDEX_${c.name} = ${c_index};
            </#list>
        }
        </#list>
    }
    </#list>
}