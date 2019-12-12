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
package org.apache.isis.config.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigUtil_isProtected_Test {

    @Test
    public void null_is_not() {
        assertFalse(ConfigUtil.isProtected(null));

        assertEquals("xxx", ConfigUtil.maskIfProtected(null, "xxx"));
    }

    @Test
    public void empty_is_not() {
        assertFalse(ConfigUtil.isProtected(""));
    }

    @Test
    public void password_is() {
        assertTrue(ConfigUtil.isProtected("foo.PassWord.bar"));
        assertTrue(ConfigUtil.isProtected("password.bar"));
        assertTrue(ConfigUtil.isProtected("foo.PASSWORD"));

        assertEquals("********", ConfigUtil.maskIfProtected("password", "xxx"));
    }

    @Test
    public void apiKey_is() {
        assertTrue(ConfigUtil.isProtected("foo.apiKey.bar"));
        assertTrue(ConfigUtil.isProtected("APIKEY.bar"));
        assertTrue(ConfigUtil.isProtected("foo.apikey"));
    }
    @Test
    public void authToken_is() {
        assertTrue(ConfigUtil.isProtected("foo.authToken.bar"));
        assertTrue(ConfigUtil.isProtected("AUTHTOKEN.bar"));
        assertTrue(ConfigUtil.isProtected("foo.authtoken"));
    }
    @Test
    public void otherwise_is_not() {
        assertFalse(ConfigUtil.isProtected("foo"));
    }
}