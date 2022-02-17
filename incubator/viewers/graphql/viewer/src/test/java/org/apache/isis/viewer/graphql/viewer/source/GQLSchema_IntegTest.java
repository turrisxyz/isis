package org.apache.isis.viewer.graphql.viewer.source;

import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.*;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.security.bypass.IsisModuleSecurityBypass;
import org.apache.isis.viewer.graphql.viewer.IsisModuleIncViewerGraphqlViewer;
import org.apache.isis.viewer.graphql.viewer.source.gqltestdomain.E1;
import org.apache.isis.viewer.graphql.viewer.source.gqltestdomain.E2;
import org.apache.isis.viewer.graphql.viewer.source.gqltestdomain.GQLTestDomainMenu;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.util.List;

import static org.apache.isis.commons.internal.assertions._Assert.*;

@SpringBootTest(
        classes = {
                IsisModuleCoreRuntimeServices.class,
                IsisModuleSecurityBypass.class,
                IsisModuleIncViewerGraphqlViewer.class,
        })
@TestPropertySource({
        IsisPresets.SilenceMetaModel,
        IsisPresets.SilenceProgrammingModel,
//        IsisPresets.UseLog4j2Test,
})
public class GQLSchema_IntegTest {

    @Inject
    private IsisSystemEnvironment isisSystemEnvironment;

    @Inject
    private SpecificationLoader specificationLoader;

    @Inject
    private GraphQlSourceForIsis graphQlSourceForIsis;

    @BeforeEach
    void beforeEach() {
        assertNotNull(isisSystemEnvironment);
        assertNotNull(specificationLoader);
        assertNotNull(graphQlSourceForIsis);
    }

    /**
     * See also: https://graphql.org/learn/schema/#input-types
     */
    @Test
    void object_types_cannot_be_used_as_input_types(){

        ObjectSpecification objectSpecification1 = specificationLoader.specForType(E1.class).get();
        assertNotNull(objectSpecification1);

        ObjectSpecification objectSpecification2 = specificationLoader.specForType(E2.class).get();
        assertNotNull(objectSpecification2);

        ObjectSpecification objectSpecification3 = specificationLoader.specForType(GQLTestDomainMenu.class).get();
        assertNotNull(objectSpecification3);

        ClassCastException thrown = Assertions.assertThrows(ClassCastException.class, () -> {
            GraphQL graphQL = graphQlSourceForIsis.graphQl();
        });
        Assertions.assertEquals("class graphql.schema.GraphQLObjectType cannot be cast to class graphql.schema.GraphQLInputType (graphql.schema.GraphQLObjectType and graphql.schema.GraphQLInputType are in unnamed module of loader 'app')", thrown.getMessage());
    }

    @Test
    @Disabled("Temp disable to illustrate the issue of input types above")
    void assert_stuff_works() {

//        _IocContainer iocContainer = isisSystemEnvironment.getIocContainer();
//        iocContainer.streamAllBeans().forEach(b->{
//            System.out.println(b.getId());
//        });

        ObjectSpecification objectSpecification1 = specificationLoader.specForType(E1.class).get();
        assertNotNull(objectSpecification1);

        ObjectSpecification objectSpecification2 = specificationLoader.specForType(E2.class).get();
        assertNotNull(objectSpecification2);

        ObjectSpecification objectSpecification3 = specificationLoader.specForType(GQLTestDomainMenu.class).get();
        assertNotNull(objectSpecification3);

        GraphQL graphQL = graphQlSourceForIsis.graphQl();
        GraphQLSchema graphQLSchema = graphQL.getGraphQLSchema();
//        List<GraphQLNamedType> allTypesAsList = graphQLSchema.getAllTypesAsList();
//        allTypesAsList.forEach(t->{
//            System.out.println(t.getName());
//        });

        assertTrue(graphQLSchema.containsType("gqltestdomain_E1"));
        assertTrue(graphQLSchema.containsType("gqltestdomain_E2"));
        assertTrue(graphQLSchema.containsType("gqltestdomain_GQLTestDomainMenu"));

        GraphQLType gqltestdomain_e1 = graphQLSchema.getType("gqltestdomain_E1");
        List<GraphQLSchemaElement> children = gqltestdomain_e1.getChildren();
        assertEquals(5, children.size());

        GraphQLObjectType gqltestdomain_e2 = (GraphQLObjectType) graphQLSchema.getType("gqltestdomain_E2");
        List<GraphQLFieldDefinition> fields = gqltestdomain_e2.getFields();
        assertEquals(9, fields.size());

        GraphQLFieldDefinition f6 = fields.get(5);
        assertEquals("otherE2List", f6.getName());
        Class<? extends GraphQLOutputType> f6TypeClass = f6.getType().getClass();
        assertEquals(GraphQLList.class, f6TypeClass);
        GraphQLList list = (GraphQLList) f6.getType();
        GraphQLTypeReference originalWrappedType = (GraphQLTypeReference) list.getOriginalWrappedType();
        assertEquals(originalWrappedType.getName(), gqltestdomain_e2.getName());

        GraphQLFieldDefinition f7 = fields.get(6);
        assertEquals("stringList", f7.getName());
        Class<? extends GraphQLOutputType> f7TypeClass = f7.getType().getClass();
        assertEquals(GraphQLList.class, f7TypeClass);
        GraphQLList list2 = (GraphQLList) f7.getType();
        GraphQLScalarType originalWrappedType2 = (GraphQLScalarType) list2.getOriginalWrappedType();
        assertEquals(Scalars.GraphQLString, originalWrappedType2);

        GraphQLFieldDefinition f8 = fields.get(7);
        assertEquals("zintList", f8.getName());
        Class<? extends GraphQLOutputType> f8TypeClass = f8.getType().getClass();
        assertEquals(GraphQLList.class, f8TypeClass);
        GraphQLList list3 = (GraphQLList) f8.getType();
        GraphQLScalarType originalWrappedType3 = (GraphQLScalarType) list3.getOriginalWrappedType();
        assertEquals(Scalars.GraphQLInt, originalWrappedType3);

        GraphQLFieldDefinition f9 = fields.get(8);
        assertEquals("otherEntities", f9.getName());
        Class<? extends GraphQLOutputType> f9TypeClass = f9.getType().getClass();
        assertEquals(GraphQLList.class, f9TypeClass);
        GraphQLList list4 = (GraphQLList) f9.getType();
        GraphQLTypeReference originalWrappedType4 = (GraphQLTypeReference) list4.getOriginalWrappedType();
        assertEquals("org_apache_isis_viewer_graphql_viewer_source_gqltestdomain_TestEntity", originalWrappedType4.getName());

        GraphQLType gqltestdomain_e1__domainObject_meta = graphQLSchema.getType("gqltestdomain_E1__DomainObject_meta");
        List<GraphQLSchemaElement> children1 = gqltestdomain_e1__domainObject_meta.getChildren();
        assertEquals(3, children1.size());

        GraphQLCodeRegistry codeRegistry = graphQLSchema.getCodeRegistry();
        assertNotNull(codeRegistry);

        // example of data fetches registered
        assertTrue(codeRegistry.hasDataFetcher(FieldCoordinates.coordinates("gqltestdomain_E1", "e2")));
        DataFetcher<?> dataFetcher = codeRegistry.getDataFetcher(FieldCoordinates.coordinates("gqltestdomain_E1", "e2"), (GraphQLFieldDefinition) gqltestdomain_e1.getChildren().get(0));
        assertNotNull(dataFetcher);


    }


}
