package org.apache.isis.viewer.graphql.viewer.source;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import java.util.ArrayList;
import java.util.List;

public class CachingGraphQLObjectTypeBuilder2 extends GraphQLObjectType.Builder {

    private GraphQLObjectType build;

    private List<GraphQLFieldDefinition.Builder> fieldBuilders = new ArrayList<>();

    @Override
    public GraphQLObjectType build() {

        if (build == null) {
            fieldBuilders.forEach(b->b.build());
            build = super.build();
        }

        return build;
    }

    @Override
    public GraphQLObjectType.Builder field(GraphQLFieldDefinition.Builder builder) {
        fieldBuilders.add(builder);
        return this;
    }

}
