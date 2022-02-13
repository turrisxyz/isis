package org.apache.isis.viewer.graphql.viewer.source;

import graphql.GraphQL;
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
import org.junit.jupiter.api.BeforeEach;
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

    @Test
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
