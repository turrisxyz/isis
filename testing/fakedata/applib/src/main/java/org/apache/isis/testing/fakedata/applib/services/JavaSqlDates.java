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
package org.apache.isis.testing.fakedata.applib.services;

import java.sql.Date;
import java.time.OffsetDateTime;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * @since 2.0 {@index}
 */
public class JavaSqlDates extends AbstractRandomValueGenerator {

    public JavaSqlDates(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    @Programmatic
    public java.sql.Date any() {
        final OffsetDateTime dateTime = fake.j8DateTimes().any();
        final Date sqldt = asSqlDate(dateTime);
        return sqldt;
    }

    private static Date asSqlDate(final OffsetDateTime dateTime) {
        long epochMillis = dateTime.toInstant().toEpochMilli();
        return new Date(epochMillis);
    }
}