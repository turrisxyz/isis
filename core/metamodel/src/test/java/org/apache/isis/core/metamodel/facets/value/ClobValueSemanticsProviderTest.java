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
package org.apache.isis.core.metamodel.facets.value;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.metamodel.valuesemantics.ClobValueSemantics;

public class ClobValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<Clob> {

    private ClobValueSemantics value;
    private Clob clob;

    @Before
    public void setUpObjects() throws Exception {
        clob = new Clob("myfile1.xml", "application", "xml", "abcdef");
        allowMockAdapterToReturn(clob);

        setSemantics(value = new ClobValueSemantics());
    }

    @Test
    public void testTitleOf() {
        assertEquals("myfile1.xml", value.titlePresentation(null, clob));
    }

    @Test
    @Override
    public void testParseNull() throws Exception {
        // disabled, clob has no parser
    }

    @Test
    @Override
    public void testParseEmptyString() throws Exception {
        // disabled, clob has no parser
    }

}
