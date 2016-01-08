/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.layout;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.v1_0.ObjectLayoutMetadata;

public interface ObjectLayoutMetadataService {

    /**
     * Returns raw (unnormalized) metadata, per the <code>.layout.xml</code> file.
     */
    @Programmatic
    ObjectLayoutMetadata fromXml(Class<?> domainClass);

    /**
     *  @param objectLayoutMetadata - the layout to be validated.
     * @param domainClass - as per domain class.
     */
    @Programmatic
    ObjectLayoutMetadata normalize(final ObjectLayoutMetadata objectLayoutMetadata, final Class<?> domainClass);

    /**
     * Obtains the layout metadata for the specified domain object.  It will have been {@link #normalize(ObjectLayoutMetadata, Class) normalized} already.
     */
    @Programmatic
    ObjectLayoutMetadata toMetadata(Object domainObject);

    /**
     * Obtains the layout metadata for the specified domain class.  It will have been {@link #normalize(ObjectLayoutMetadata, Class) normalized} already.
     */
    @Programmatic
    ObjectLayoutMetadata toMetadata(Class<?> domainClass);

}