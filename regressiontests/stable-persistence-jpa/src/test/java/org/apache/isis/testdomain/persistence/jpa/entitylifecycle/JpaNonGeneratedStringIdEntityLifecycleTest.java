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
package org.apache.isis.testdomain.persistence.jpa.entitylifecycle;

import java.util.Objects;

import javax.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.jpa.entities.JpaEntityNonGeneratedStringId;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
        },
        properties = {
        })
@Transactional
@TestPropertySource(IsisPresets.UseLog4j2Test)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@DirtiesContext
class JpaNonGeneratedStringIdEntityLifecycleTest {

    @Inject private RepositoryService repository;
    @Inject private ObjectManager objectManager;
    @Inject private KVStoreForTesting kvStore;

    @Test @Order(0) @Commit
    void cleanup_justInCase() {
        // cleanup just in case
        repository.removeAll(JpaEntityNonGeneratedStringId.class);
    }

    @Test @Order(1) @Commit
    void detached_shouldBeProperlyDetected() {

        val entity = objectManager.adapt(
                repository.detachedEntity(new JpaEntityNonGeneratedStringId("test")));

        assertTrue(entity.getSpecification().isEntity());
        assertEquals(
                EntityState.PERSISTABLE_DETACHED,
                ManagedObjects.EntityUtil.getEntityState(entity));

        setEntityRef(entity);
    }

    @Test @Order(2) @Commit
    void attached_shouldBeProperlyDetected() {

        val entity = getEntityRef();

        repository.persist(entity.getPojo());

        assertEquals(
                EntityState.PERSISTABLE_ATTACHED,
                ManagedObjects.EntityUtil.getEntityState(entity));
        assertEquals(1, repository.allInstances(JpaEntityNonGeneratedStringId.class).size());

    }

    @Test @Order(3) @Commit
    void removed_shouldBeProperlyDetected() {

        // expected post-condition (after persist, and having entered a new transaction)
        assertEquals(
                EntityState.PERSISTABLE_DETACHED,
                ManagedObjects.EntityUtil.getEntityState(getEntityRef()));

        val id = ((JpaEntityNonGeneratedStringId)getEntityRef().getPojo()).getName();

        val entity = objectManager.adapt(
                repository.firstMatch(
                        JpaEntityNonGeneratedStringId.class,
                        entityPojo->Objects.equals(entityPojo.getName(), id))
                .orElseThrow(_Exceptions::noSuchElement));

        // expected pre-condition (before removal)
        assertEquals(
                EntityState.PERSISTABLE_ATTACHED,
                ManagedObjects.EntityUtil.getEntityState(entity));

        repository.remove(entity.getPojo());

        // expected post-condition (after removal)
        assertDetachedOrDeleted(entity);

        setEntityRef(entity);
    }

    @Test @Order(4) @Commit
    void postCondition_shouldBe_anEmptyRepository() {

        val entity = getEntityRef();

        assertDetachedOrDeleted(entity);
        assertEquals(0, repository.allInstances(JpaEntityNonGeneratedStringId.class).size());

    }

    @Test @Order(5)
    void cleanup() {
        kvStore.clear(this);
    }

    // -- HELPER

    void setEntityRef(final ManagedObject entity) {
        kvStore.put(this, "entity", entity);
    }

    ManagedObject getEntityRef() {
        val entity = (ManagedObject) kvStore.get(this, "entity").get();
        return entity;
    }

    static void assertDetachedOrDeleted(final ManagedObject entity) {
        assertEquals(
                EntityState.PERSISTABLE_DETACHED, // if undecidable we currently return PERSISTABLE_DETACHED;
                ManagedObjects.EntityUtil.getEntityState(entity));
    }


}
