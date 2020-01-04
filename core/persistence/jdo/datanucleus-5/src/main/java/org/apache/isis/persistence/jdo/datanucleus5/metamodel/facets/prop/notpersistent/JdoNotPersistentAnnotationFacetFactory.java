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
package org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.prop.notpersistent;

import javax.jdo.annotations.NotPersistent;

import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.JdoMetamodelUtil;


public class JdoNotPersistentAnnotationFacetFactory extends FacetFactoryAbstract {

    public JdoNotPersistentAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(ProcessMethodContext processMethodContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processMethodContext.getCls();
        if(!JdoMetamodelUtil.isPersistenceEnhanced(cls)) {
            return;
        }
        
        //val method = processMethodContext.getMethod();

        final NotPersistent annotation = processMethodContext.synthesizeOnMethod(NotPersistent.class)
                .orElse(null);
                
//        _Assert.assertEquals("expected same", annotation,
//                Annotations.getAnnotation(method, NotPersistent.class));

        if (annotation == null) {
            return;
        }

        final FacetedMethod holder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JdoNotPersistentFacetAnnotation(holder));
        FacetUtil.addFacet(new NotPersistedFacetDerivedFromJdoNotPersistentAnnotation(holder));
    }
}
