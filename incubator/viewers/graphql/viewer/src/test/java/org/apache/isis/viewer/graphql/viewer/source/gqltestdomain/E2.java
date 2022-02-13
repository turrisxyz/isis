package org.apache.isis.viewer.graphql.viewer.source.gqltestdomain;

import lombok.Getter;
import lombok.Setter;
import org.apache.isis.applib.annotation.*;

import javax.inject.Inject;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @OneToMany
    @Getter @Setter
    private List<E2> otherE2List = new ArrayList<>();

    @Getter @Setter
    @Collection
    private List<String> stringList = new ArrayList<>();

    @Getter @Setter
    @Collection
    private List<Integer> zintList = new ArrayList<>();

    @Action(semantics = SemanticsOf.SAFE)
    public List<TestEntity> otherEntities(){
        List<TestEntity> result = new ArrayList<>();
        result.addAll(testEntityRepository.findAllE1());
        result.addAll(testEntityRepository.findAllE2().stream().filter(e2->e2!=this).collect(Collectors.toList()));
        return result;
    }

    @Inject
    @Transient
    TestEntityRepository testEntityRepository;

}
