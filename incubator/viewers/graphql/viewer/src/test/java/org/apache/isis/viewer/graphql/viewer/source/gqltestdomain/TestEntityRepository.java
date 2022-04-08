package org.apache.isis.viewer.graphql.viewer.source.gqltestdomain;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TestEntityRepository {

    @Inject
    private RepositoryService repository;

    public E1 createE1(final String name, @Nullable final E2 e2) {
        E1 e1 = new E1();
        e1.setName(name);
        e1.setE2(e2);
        repository.persistAndFlush(e1);
        return e1;
    }

    public E2 createE2(final String name, @Nullable final E1 e1) {
        E2 e2 = new E2();
        e2.setName(name);
        e2.setE1(e1);
        repository.persistAndFlush(e2);
        return e2;
    }

    public List<E1> findAllE1() {
        return repository.allInstances(E1.class);
    }

    public List<E2> findAllE2() {
        return repository.allInstances(E2.class);
    }

    public List<TestEntity> findAllTestEntities() {
        final List<TestEntity> result = new ArrayList<>();
        result.addAll(findAllE1());
        result.addAll(findAllE2());
        return result;
    }

}
