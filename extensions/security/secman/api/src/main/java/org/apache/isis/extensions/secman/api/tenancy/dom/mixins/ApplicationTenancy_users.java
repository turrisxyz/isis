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
package org.apache.isis.extensions.secman.api.tenancy.dom.mixins;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy;
import org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy.CollectionDomainEvent;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

@Collection(
        domainEvent = ApplicationTenancy_users.DomainEvent.class)
@CollectionLayout(
        defaultView="table"
)
@RequiredArgsConstructor
public class ApplicationTenancy_users {

    @Inject private ApplicationUserRepository applicationUserRepository;

    private final ApplicationTenancy target;


    public static class DomainEvent
            extends CollectionDomainEvent<ApplicationUser> {}

    public java.util.Collection<ApplicationUser> coll() {
        return applicationUserRepository.findByAtPath(target.getPath());
    }

}