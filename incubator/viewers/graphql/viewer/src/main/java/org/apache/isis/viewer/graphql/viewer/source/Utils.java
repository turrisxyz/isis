package org.apache.isis.viewer.graphql.viewer.source;

public class Utils {

    public static String metaTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_meta";
    }

    public static String logicalTypeNameSanitized(final String logicalTypeName) {
        return logicalTypeName.replace('.', '_');
    }

}
