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
package org.apache.isis.security.bypass.authorization;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.core.security.authorization.Authorizor;

/**
 * @since 1.x {@index}
 */
@Service
@Named("isis.security.AuthorizorBypass")
@javax.annotation.Priority(PriorityPrecedence.LATE)
@Qualifier("Bypass")
public class AuthorizorBypass implements Authorizor {

    @Override
    public boolean isVisible(final InteractionContext authentication, final Identifier identifier) {
        return true;
    }

    @Override
    public boolean isUsable(final InteractionContext authentication, final Identifier identifier) {
        return true;
    }

}
