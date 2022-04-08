package org.apache.isis.viewer.graphql.viewer.source;

import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.graphql.viewer.source.gqltestdomain.TestEntityRepository;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.TextWebReporter;
import org.approvaltests.reporters.UseReporter;
import org.approvaltests.reporters.macosx.MacDiffReporter;
import org.approvaltests.scrubbers.RegExScrubber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import static org.apache.isis.commons.internal.assertions._Assert.assertNotNull;


@Transactional
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
    @UseReporter(TextWebReporter.class)
    void print_schema_works() throws Exception {

        HttpClient client = HttpClient.newBuilder().build();
        URI uri = URI.create("http://0.0.0.0:" + port + "/graphql/schema");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        File targetFile1 = new File("src/test/resources/testfiles/targetFile1.gql");
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(targetFile1.toPath()));

        Approvals.verify(targetFile1, new Options());

    }


}
