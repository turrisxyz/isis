package org.apache.isis.viewer.graphql.viewer.source;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import java.util.ArrayList;
import java.util.List;

public class OurGraphQLFieldDefinitionBuilder2 extends GraphQLFieldDefinition.Builder {

    private GraphQLFieldDefinition build;

    private List<GraphQLObjectType.Builder> referringBuilders = new ArrayList<>();

    @Override
    public GraphQLFieldDefinition build() {

        if (build == null) {
            referringBuilders.forEach(b->b.build());
            build = super.build();
        }

        return build;
    }

    @Override
    public GraphQLFieldDefinition.Builder type(GraphQLObjectType.Builder builder) {
        referringBuilders.add(builder);
        return this;
    }

}
