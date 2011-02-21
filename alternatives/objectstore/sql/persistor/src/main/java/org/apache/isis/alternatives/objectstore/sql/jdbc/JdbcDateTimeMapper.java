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


package org.apache.isis.alternatives.objectstore.sql.jdbc;

import org.apache.isis.alternatives.objectstore.sql.DatabaseConnector;
import org.apache.isis.alternatives.objectstore.sql.mapping.FieldMapping;
import org.apache.isis.alternatives.objectstore.sql.mapping.FieldMappingFactory;
import org.apache.isis.applib.value.DateTime;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;


public class JdbcDateTimeMapper extends AbstractJdbcFieldMapping {
    
    public static class Factory implements FieldMappingFactory {
        public FieldMapping createFieldMapping(final ObjectAssociation field) {
            return new JdbcDateTimeMapper(field);
        }
    }

    protected JdbcDateTimeMapper(final ObjectAssociation field) {
        super(field);
    }

    //TODO:KAM:here XYZ
    public String valueAsDBString(final ObjectAdapter value, DatabaseConnector connector) {
    	DateTime asDate = (DateTime) value.getObject();
    	java.sql.Timestamp xxx = new java.sql.Timestamp(asDate.longValue());
    	connector.addToQueryValues(xxx);
    	return "?";
    	/*
        EncodableFacet encodeableFacet = value.getSpecification().getFacet(EncodableFacet.class);
        String encodedString = encodeableFacet.toEncodedString(value);
        String year = encodedString.substring(0, 4);
        String month = encodedString.substring(4, 6);
        String day = encodedString.substring(6, 8);
        String hour = encodedString.substring(9, 11);
        String minute = encodedString.substring(11, 13);
        String encodedWithAdaptions = year + "-" + month + "-" + day + " " + hour + ":" + minute+ ":00000";
        return "'" + encodedWithAdaptions + "'";
        */
    }

    public ObjectAdapter setFromDBColumn(final String encodedValue, final ObjectAssociation field)             {
        // convert date to yyyymmddhhmm
        String year = encodedValue.substring(0, 4);
        String month = encodedValue.substring(5, 7);
        String day = encodedValue.substring(8, 10);
        String hour = encodedValue.substring(11, 13);
        String minute = encodedValue.substring(14, 16);
        String valueString = year + month + day + "T" + hour + minute + "00000";
        return field.getSpecification().getFacet(EncodableFacet.class).fromEncodedString(valueString);
    }
    
    public String columnType() {
        return JdbcConnector.TYPE_DATETIME;
    }

}
