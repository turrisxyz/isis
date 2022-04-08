package org.apache.isis.viewer.graphql.viewer.source;

import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.functional.ThrowingRunnable;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.graphql.viewer.source.gqltestdomain.E1;
import org.apache.isis.viewer.graphql.viewer.source.gqltestdomain.E2;
import org.apache.isis.viewer.graphql.viewer.source.gqltestdomain.TestEntityRepository;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.reporters.TextWebReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;

import static java.net.http.HttpRequest.BodyPublishers.ofFile;
import static org.apache.isis.commons.internal.assertions._Assert.assertNotNull;
import static org.apache.isis.commons.internal.assertions._Assert.assertTrue;


//@Transactional
public class EndToEnd_IntegTest extends TestDomainModuleIntegTestAbstract{

    @Inject
    private IsisSystemEnvironment isisSystemEnvironment;

    @Inject
    private SpecificationLoader specificationLoader;

    @Inject
    private GraphQlSourceForIsis graphQlSourceForIsis;

    @Inject
    TestEntityRepository testEntityRepository;

    @BeforeEach
    void beforeEach() {
        assertNotNull(isisSystemEnvironment);
        assertNotNull(specificationLoader);
        assertNotNull(graphQlSourceForIsis);
    }

    @Test
    @Disabled("Creates schema.gql file for convenience")
    void print_schema_works() throws Exception {

        HttpClient client = HttpClient.newBuilder().build();
        URI uri = URI.create("http://0.0.0.0:" + port + "/graphql/schema");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        File targetFile1 = new File("src/test/resources/testfiles/schema.gql");
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(targetFile1.toPath()));

    }

    @Test
    @UseReporter(TextWebReporter.class)
    void simple_post_request() throws Exception {

        File body1 = new File("src/test/resources/testfiles/body1.gql");
        HttpClient client = HttpClient.newBuilder().build();
        URI uri = URI.create("http://0.0.0.0:" + port + "/graphql");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(ofFile(body1.toPath())).setHeader("Content-Type", "application/json").build();
        File targetFile2 = new File("src/test/resources/testfiles/targetFile2.gql");
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(targetFile2.toPath()));

        Approvals.verify(targetFile2, new Options());

    }

    @Inject
    TransactionService transactionService;

    @Test
    @UseReporter(TextWebReporter.class)
    void findAllE1() throws Exception {

        // given
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            E1 foo = testEntityRepository.createE1("foo", null);
            testEntityRepository.createE2("bar", foo);
            transactionService.flushTransaction();
            List<E1> allE1 = testEntityRepository.findAllE1();
            assertTrue(allE1.size()==1);
            List<E2> allE2 = testEntityRepository.findAllE2();
            assertTrue(allE2.size()==1);
        });



        // when
        File body2 = new File("src/test/resources/testfiles/body2.gql");
        HttpClient client = HttpClient.newBuilder().build();
        URI uri = URI.create("http://0.0.0.0:" + port + "/graphql");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(ofFile(body2.toPath())).setHeader("Content-Type", "application/json").build();
        File targetFile3 = new File("src/test/resources/testfiles/targetFile3.gql");
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(targetFile3.toPath()));

        Approvals.verify(targetFile3, new Options());

    }


}
