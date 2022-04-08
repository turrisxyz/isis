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
import org.junit.jupiter.api.AfterEach;
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
import java.util.ArrayList;
import java.util.List;

import static java.net.http.HttpRequest.BodyPublishers.ofFile;
import static org.apache.isis.commons.internal.assertions._Assert.*;


//@Transactional NOT USING @Transactional since we are running server within same transaction otherwise
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

    @AfterEach
    void afterEach(){
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            testEntityRepository.removeAll();
        });
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
        File targetFile1 = new File("src/test/resources/testfiles/targetFile1.gql");
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(targetFile1.toPath()));

        Approvals.verify(targetFile1, new Options());

    }

    @Inject
    TransactionService transactionService;

    @Test
    @UseReporter(TextWebReporter.class)
    void findAllE1() throws Exception {

        // given
        transactionService.runTransactional(Propagation.REQUIRED, () -> {
            E1 foo = testEntityRepository.createE1("foo", null);
            testEntityRepository.createE2("bar", null);
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
        File targetFile2 = new File("src/test/resources/testfiles/targetFile2.gql");
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(targetFile2.toPath()));

        Approvals.verify(targetFile2, new Options());

    }

    @Test
    @UseReporter(TextWebReporter.class)
    void createE1() throws Exception {

        File body3 = new File("src/test/resources/testfiles/body3.gql");
        HttpClient client = HttpClient.newBuilder().build();
        URI uri = URI.create("http://0.0.0.0:" + port + "/graphql");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(ofFile(body3.toPath())).setHeader("Content-Type", "application/json").build();
        File targetFile3 = new File("src/test/resources/testfiles/targetFile3.gql");

        transactionService.runTransactional(Propagation.REQUIRED, () -> {

            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(targetFile3.toPath()));
            // just to show we need to query in separate tranasction
            List<E2> list = testEntityRepository.findAllE2();
            assertTrue(list.isEmpty());

        });

        final List<E1> allE1 = new ArrayList<>();
        transactionService.runTransactional(Propagation.REQUIRED, () -> {

            List<E1> all = testEntityRepository.findAllE1();
            allE1.addAll(all);

        });

        assertEquals(1, allE1.size());
        assertEquals("newbee", allE1.get(0).getName());
        Approvals.verify(targetFile3, new Options());

    }


}
