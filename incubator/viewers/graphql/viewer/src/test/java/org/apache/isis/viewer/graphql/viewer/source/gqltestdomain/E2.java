package org.apache.isis.viewer.graphql.viewer.source.gqltestdomain;

import lombok.Getter;
import lombok.Setter;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;

//@Profile("demo-jpa")
@Entity
@Table(
        schema = "demo",
        name = "E2"
)
@DomainObject(nature = Nature.ENTITY, logicalTypeName = "gqltestdomain.E2")
public class E2 implements TestEntity{

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    @Property
    @ManyToOne(optional = true)
    @JoinColumn(name = "e1_id")
    private E1 e1;

}
