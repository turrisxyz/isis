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
package org.apache.isis.viewer.restfulobjects.jaxrsresteasy4;

import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.specimpl.ResteasyUriBuilderImpl;
import org.jboss.resteasy.spi.Failure;

import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.client.UriBuilderPlugin;
import org.apache.isis.viewer.restfulobjects.viewer.IsisJaxrsServerPlugin;

public class IsisResteasy4Plugin implements UriBuilderPlugin, IsisJaxrsServerPlugin {

    @Override
    public UriBuilder uriTemplate(String uriTemplate) {
        return new ResteasyUriBuilderImpl().uriTemplate(uriTemplate);
    }


    @Override
    public HttpStatusCode getFailureStatusCodeIfAny(Throwable ex) {

        return (ex instanceof Failure)
                ? RestfulResponse.HttpStatusCode.statusFor(((Failure)ex).getErrorCode())
                        : null;
    }

}
