package org.apache.isis.viewer.graphql.viewer.source;

import graphql.Assert;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

public class CachingGraphQLObjectTypeBuilder extends GraphQLObjectType.Builder {

    private GraphQLObjectType build;

    @Override
    public GraphQLObjectType build() {

        if (build == null) build = super.build();

        return build;
    }

    @Override
    public GraphQLObjectType.Builder field(graphql.schema.GraphQLFieldDefinition.Builder builder) {
        return super.field(builder.build());
    }

}
