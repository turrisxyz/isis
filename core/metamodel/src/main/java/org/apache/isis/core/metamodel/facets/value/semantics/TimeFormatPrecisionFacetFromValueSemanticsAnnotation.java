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
package org.apache.isis.core.metamodel.facets.value.semantics;

import java.util.Optional;

import org.apache.isis.applib.annotation.TimePrecision;
import org.apache.isis.applib.annotation.ValueSemantics;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.temporalformat.TimeFormatPrecisionFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.temporalformat.TimeFormatPrecisionFacetAbstract;

public class TimeFormatPrecisionFacetFromValueSemanticsAnnotation
extends TimeFormatPrecisionFacetAbstract {

    public static Optional<TimeFormatPrecisionFacet> create(
            final Optional<ValueSemantics> valueSemanticsIfAny,
            final FacetHolder holder) {

        return valueSemanticsIfAny
        .map(ValueSemantics::timePrecision)
        .map(timePrecision->
            new TimeFormatPrecisionFacetFromValueSemanticsAnnotation(timePrecision, holder));
   }

   private TimeFormatPrecisionFacetFromValueSemanticsAnnotation(
           final TimePrecision timePrecision, final FacetHolder holder) {
       super(timePrecision, holder);
   }

}
