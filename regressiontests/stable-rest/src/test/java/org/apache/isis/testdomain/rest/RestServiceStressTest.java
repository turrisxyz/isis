/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.testdomain.rest;

import java.util.stream.IntStream;

import javax.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.commons.internal.base._Timing;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.extensions.restclient.RestfulClient;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.util.rest.RestEndpointService;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@SpringBootTest(
        classes = {RestEndpointService.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Import({
    Configuration_usingJdo.class,
    IsisModuleViewerRestfulObjectsJaxrsResteasy4.class
})
@Log4j2
class RestServiceStressTest {

    @LocalServerPort int port; // just for reference (not used)
    @Inject RestEndpointService restService;

    @Test @Disabled("test works, though don't enable when building")
    void bookOfTheWeek_stressTest() {

        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = false;
        final int clients = 16;
        final int iterations = 1000;
        val label = String.format("Calling REST endpoint %d times", clients * iterations);

        _Timing.runVerbose(log, label, ()->{

            IntStream.range(0, clients)
            .parallel()
            .mapToObj(i->{
                val restfulClient = restService.newClient(useRequestDebugLogging);
                return restfulClient;
            })
            .forEach(restfulClient->{

                IntStream.range(0, iterations)
                .forEach(iter->{
                    requestSingleBookOfTheWeek_viaRestEndpoint(restfulClient);
                });

            });

        });
    }

    void requestSingleBookOfTheWeek_viaRestEndpoint(final RestfulClient restfulClient) {

        val digest = restService.getRecommendedBookOfTheWeekDto(restfulClient);

        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }

        val bookOfTheWeek = digest.getEntities().getSingletonOrFail();

        assertNotNull(bookOfTheWeek);
        assertEquals("Book of the week", bookOfTheWeek.getName());
    }

}
