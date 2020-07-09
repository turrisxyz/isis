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
package demoapp.dom.types.isis.clobs.holder;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.value.Clob;

//tag::class[]
public interface IsisClobHolder2 extends IsisClobHolder {

    @Property
    @PropertyLayout(                                                        // <.>
            labelPosition = LabelPosition.LEFT,
            describedAs = "labelPosition=LEFT",
            hidden = Where.ALL_TABLES
    )
    @MemberOrder(name = "label-positions", sequence = "1")                  // <.>
    default Clob getReadOnlyPropertyDerivedLabelPositionLeft() {
        return getReadOnlyProperty();
    }

    @Property
    @PropertyLayout(
            labelPosition = LabelPosition.TOP,
            describedAs = "labelPosition=TOP",
            hidden = Where.ALL_TABLES
    )
    @MemberOrder(name = "label-positions", sequence = "2")
    default Clob getReadOnlyPropertyDerivedLabelPositionTop() {
        return getReadOnlyProperty();
    }

    @Property
    @PropertyLayout(
            labelPosition = LabelPosition.RIGHT,    // TODO: not honoured
            describedAs = "labelPosition=RIGHT",
            hidden = Where.ALL_TABLES
    )
    @MemberOrder(name = "label-positions", sequence = "3")
    default Clob getReadOnlyPropertyDerivedLabelPositionRight() {
        return getReadOnlyProperty();
    }

    @Property
    @PropertyLayout(
            labelPosition = LabelPosition.NONE,
            describedAs = "labelPosition=NONE",
            hidden = Where.ALL_TABLES
    )
    @MemberOrder(name = "label-positions", sequence = "4")
    default Clob getReadOnlyPropertyDerivedLabelPositionNone() {
        return getReadOnlyProperty();
    }

}
//end::class[]