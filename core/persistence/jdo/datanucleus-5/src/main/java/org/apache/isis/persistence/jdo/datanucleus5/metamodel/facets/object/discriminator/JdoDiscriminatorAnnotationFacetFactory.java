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

package org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.object.discriminator;

import javax.inject.Inject;
import javax.jdo.annotations.Discriminator;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.ObjectSpecIdFacetFactory;
import org.apache.isis.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.metamodel.facets.object.objectspecid.classname.ObjectSpecIdFacetDerivedFromClassName;
import org.apache.isis.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.isis.persistence.jdo.datanucleus5.metamodel.JdoMetamodelUtil;

public class JdoDiscriminatorAnnotationFacetFactory
extends FacetFactoryAbstract
implements ObjectSpecIdFacetFactory {

    @Inject
    private ClassSubstitutor classSubstitutor;

    public JdoDiscriminatorAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessObjectSpecIdContext processClassContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processClassContext.getCls();
        if(!JdoMetamodelUtil.isPersistenceEnhanced(cls)) {
            return;
        }

        final Discriminator annotation = Annotations.getAnnotation(cls, Discriminator.class);
        if (annotation == null) {
            return;
        }
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final String annotationValue = annotation.value();
        final ObjectSpecIdFacet facet;
        if (!_Strings.isNullOrEmpty(annotationValue)) {
            facet = new ObjectSpecIdFacetInferredFromJdoDiscriminatorValueAnnotation(
                    annotationValue, facetHolder);
        } else {
            final Class<?> substitutedClass = classSubstitutor.getClass(cls);
            facet = substitutedClass != null
                        ? new ObjectSpecIdFacetDerivedFromClassName(substitutedClass.getCanonicalName(), facetHolder)
                        : null;
        }
        FacetUtil.addFacet(facet);
    }


    @Override
    public void process(ProcessClassContext processClassContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processClassContext.getCls();
        if(!JdoMetamodelUtil.isPersistenceEnhanced(cls)) {
            return;
        }

        final Discriminator annotation = Annotations.getAnnotation(processClassContext.getCls(), Discriminator.class);
        if (annotation == null) {
            return;
        }
        String annotationValueAttribute = annotation.value();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        FacetUtil.addFacet(new JdoDiscriminatorFacetDefault(annotationValueAttribute, facetHolder));
    }

}
