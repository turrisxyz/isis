package org.apache.isis.viewer.graphql.viewer.source;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.validation.InvalidSchemaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;

class ScratchPad {

    @Test
    public void simple_case() {

        // given
        GraphQLObjectType query = getQuery();

        final Map<String, GraphQLObjectType.Builder> entityBuilders = new HashMap<>();

        GraphQLObjectType.Builder eb1 = new CachingGraphQLObjectTypeBuilder().name("e1");
        eb1.field(GraphQLFieldDefinition.newFieldDefinition().name("e1f1").type(Scalars.GraphQLString).build());
        entityBuilders.put("e1", eb1);

        GraphQLObjectType.Builder eb2 = new CachingGraphQLObjectTypeBuilder().name("e2");
        eb2.field(GraphQLFieldDefinition.newFieldDefinition().name("e2f1").type(Scalars.GraphQLString).build());
        entityBuilders.put("e2", eb2);

        // when
        Set<GraphQLType> entityTypes = new HashSet<>();
        entityBuilders.forEach((k, b) -> entityTypes.add(b.build()));

        // then

        GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(query)
                .additionalTypes(entityTypes)
//                .codeRegistry(codeRegistry)
                .build();


        // then
        Assertions.assertTrue(schema.containsType("e1"));
        GraphQLFieldDefinition fde1 = (GraphQLFieldDefinition) schema.getType("e1").getChildren().get(0);
        Assertions.assertEquals("e1f1", fde1.getName());
        Assertions.assertEquals(Scalars.GraphQLString, fde1.getType());

        Assertions.assertTrue(schema.containsType("e2"));
        GraphQLFieldDefinition fde2 = (GraphQLFieldDefinition) schema.getType("e2").getChildren().get(0);
        Assertions.assertEquals("e2f1", fde2.getName());
        Assertions.assertEquals(Scalars.GraphQLString, fde2.getType());

        Assertions.assertTrue(schema.containsType("query"));
        GraphQLFieldDefinition fdq = (GraphQLFieldDefinition) schema.getType("query").getChildren().get(0);
        Assertions.assertEquals("f1", fdq.getName());
        Assertions.assertEquals(Scalars.GraphQLString, fdq.getType());

    }

    private GraphQLObjectType getQuery() {
        GraphQLObjectType.Builder queryBuilder = new CachingGraphQLObjectTypeBuilder().name("query");
        queryBuilder.field(GraphQLFieldDefinition.newFieldDefinition().name("f1").type(Scalars.GraphQLString).build());
        GraphQLObjectType query = queryBuilder.build();
        return query;
    }

    @Test
    public void nested_builders_work_with_caching_object_type_builder_and_field_definition_builder_registry_no_forward_reference() {

        // given
        GraphQLObjectType query = getQuery();

        final Map<String, GraphQLObjectType.Builder> typeBuilderRegistry = new HashMap<>();
        final List<GraphQLFieldDefinition.Builder> fieldDefinitionBuilderRegistry = new ArrayList<>();

        // registering builder for e1
        GraphQLObjectType.Builder eb1_referencing_e3 = new CachingGraphQLObjectTypeBuilder().name("e1");
        GraphQLFieldDefinition.Builder e1f1 = GraphQLFieldDefinition.newFieldDefinition().name("e1f1").type(Scalars.GraphQLString);
        fieldDefinitionBuilderRegistry.add(e1f1);
        eb1_referencing_e3.field(e1f1);
        typeBuilderRegistry.put("e1", eb1_referencing_e3);

        // registering builder for e2 referencing e1
        GraphQLObjectType.Builder eb2_referecing_e1 = new CachingGraphQLObjectTypeBuilder().name("e2");
        // we encounter e1 and already we have a builder to use in the field def
        GraphQLFieldDefinition.Builder e2f1b = GraphQLFieldDefinition.newFieldDefinition().name("e2f1").type(typeBuilderRegistry.get("e1"));
        // we register the field def
        fieldDefinitionBuilderRegistry.add(e2f1b);
        // we add the field and register e2
        eb2_referecing_e1.field(e2f1b);
        typeBuilderRegistry.put("e2", eb2_referecing_e1);


        // when
        fieldDefinitionBuilderRegistry.forEach(b -> b.build());
        Set<GraphQLType> entityTypes = new HashSet<>();
        typeBuilderRegistry.forEach((k, b) -> entityTypes.add(b.build()));

        // then

        GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(query)
                .additionalTypes(entityTypes)
                .build();

        GraphQLType e2 = schema.getType("e2");
        Assertions.assertEquals(1, e2.getChildren().size());
        GraphQLFieldDefinition graphQLSchemaElement = (GraphQLFieldDefinition) e2.getChildren().get(0);
        Assertions.assertEquals("e2f1", graphQLSchemaElement.getName());
        Assertions.assertEquals(schema.getType("e1"), graphQLSchemaElement.getType());

    }


    @Test
    /**
     * I think this test shows that, using the current schema builder pattern it is impossible to have circular references
     * (here: e1 references e2, e2 references e1)
     *
     * the moment a type is referenced in a field definition builder, it is built and stored as an Object Type in that builder
     * also: the moment a field is added to an object type builder, it is built and stored as a Field Definition in the type builder
     *
     * suppose the API allows us to replace all these built objects before building the schema, in this example we would be in an infinite loop ...
     * Maybe try build the schema first (with some temporary replacement types - in this case a replacement for e2 when building e1 - and then try to replace afterwards?
     * There is something like GraphQLObjectType.Builder replaceFields(List<GraphQLFieldDefinition> fieldDefinitions)
     *
     */
    public void nested_builders_work_with_caching_object_type_builder_and_field_definition_builder_registry_does_not_support_circular_references() {

        // given
        GraphQLObjectType query = getQuery();

        final Map<String, GraphQLObjectType.Builder> typeBuilderRegistry = new HashMap<>();
        final List<GraphQLFieldDefinition.Builder> fieldDefinitionBuilderRegistry = new ArrayList<>();

        // registering builder for e1 referencing e2
        CachingGraphQLObjectTypeBuilder eb1_referencing_e2 = (CachingGraphQLObjectTypeBuilder) new CachingGraphQLObjectTypeBuilder().name("e1");
        // we encounter e2, so we register a builder for e2
        CachingGraphQLObjectTypeBuilder eb2_referencing_e1 = (CachingGraphQLObjectTypeBuilder) new CachingGraphQLObjectTypeBuilder().name("e2");
        typeBuilderRegistry.put("e2", eb2_referencing_e1);

        // we put the buider for e2 in the field def and register the field def
        // PROBLEMATIC LINE
        GraphQLFieldDefinition.Builder e1f1 = GraphQLFieldDefinition.newFieldDefinition().name("e1f1").type(typeBuilderRegistry.get("e2")); // <=== by calling type an object is built; we would like to defer that
        // this is the same as
//        GraphQLFieldDefinition.newFieldDefinition().name("e1f1").type(typeBuilderRegistry.get("e2").build());

        fieldDefinitionBuilderRegistry.add(e1f1); // useless since object type builders do not refer
        // we add the field to the builder of e1 and register the builder
        eb1_referencing_e2.field(e1f1); // <== PROBLEM is same as eb1_referencing_e2.field(e1f1.build())
        typeBuilderRegistry.put("e1", eb1_referencing_e2);

        // we now build the rest of e2 referencing e1 (circular reference)
        GraphQLFieldDefinition.Builder e2f1 = GraphQLFieldDefinition.newFieldDefinition().name("e2f1").type(typeBuilderRegistry.get("e1"));
        // we register the field def
        fieldDefinitionBuilderRegistry.add(e2f1);
        // we add the field
        eb2_referencing_e1.field(e2f1);


        // when
        fieldDefinitionBuilderRegistry.forEach(b -> b.build());
        Set<GraphQLType> entityTypes = new HashSet<>();
        typeBuilderRegistry.forEach((k, b) -> entityTypes.add(b.build()));

        // then

        InvalidSchemaException exception = Assertions.assertThrowsExactly(InvalidSchemaException.class, () -> {
            GraphQLSchema schema = GraphQLSchema.newSchema()
                    .query(query)
                    .additionalTypes(entityTypes)
                    .build();
        });
        Assertions.assertEquals("invalid schema:\n" +
                "\"e2\" must define one or more fields.", exception.getMessage());


    }

    @Test()
    @Disabled("Produces stack overflow")
    public void nested_builders_work_with_caching_object_type_builder2_produces_stackoverflow() {

        // given
        GraphQLObjectType query = getQuery();

        final Map<String, GraphQLObjectType.Builder> typeBuilderRegistry = new HashMap<>();

        // registering builder for e1 referencing e2
        CachingGraphQLObjectTypeBuilder2 eb1_referencing_e2 = (CachingGraphQLObjectTypeBuilder2) new CachingGraphQLObjectTypeBuilder2().name("e1");
        // we encounter e2, so we register a builder for e2
        CachingGraphQLObjectTypeBuilder2 eb2_referencing_e1 = (CachingGraphQLObjectTypeBuilder2) new CachingGraphQLObjectTypeBuilder2().name("e2");
        typeBuilderRegistry.put("e2", eb2_referencing_e1);

        // we put the buider for e2 in the field def and register the field def
        OurGraphQLFieldDefinitionBuilder2 e1f1 = (OurGraphQLFieldDefinitionBuilder2) new OurGraphQLFieldDefinitionBuilder2().name("e1f1").type(typeBuilderRegistry.get("e2"));
        eb1_referencing_e2.field(e1f1);
        typeBuilderRegistry.put("e1", eb1_referencing_e2);

        // we now build the rest of e2 referencing e1 (circular reference)
        OurGraphQLFieldDefinitionBuilder2 e2f1 = (OurGraphQLFieldDefinitionBuilder2) new OurGraphQLFieldDefinitionBuilder2().name("e2f1").type(typeBuilderRegistry.get("e1"));
        eb2_referencing_e1.field(e2f1);

        // when
        Set<GraphQLType> entityTypes = new HashSet<>();
        typeBuilderRegistry.forEach((k, b) -> entityTypes.add(b.build()));

        // then


        GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(query)
                .additionalTypes(entityTypes)
                .build();


    }

}