package org.apache.isis.viewer.graphql.viewer.source;

import graphql.Scalars;
import graphql.schema.*;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ObjectTypeFactory {

    private final BookmarkService bookmarkService;
    private final SpecificationLoader specificationLoader;

    public void objectTypeFromObjectSpecification(final ObjectSpecification objectSpecification, final Set<GraphQLType> graphQLObjectTypes, final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        val logicalTypeName = objectSpecification.getLogicalTypeName();
        String logicalTypeNameSanitized = logicalTypeNameSanitized(logicalTypeName);


        GraphQLObjectType.Builder objectTypeBuilder = newObject().name(logicalTypeNameSanitized);

        objectSpecification.streamProperties(MixedIn.INCLUDED)
                .forEach(otoa -> {

                    ObjectSpecification fieldObjectSpecification = otoa.getElementType();
                    BeanSort beanSort = fieldObjectSpecification.getBeanSort();
                    switch (beanSort) {

                        case VIEW_MODEL:
                        case ENTITY:

                            String logicalTypeNameOfField = fieldObjectSpecification.getLogicalTypeName();

                            GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition().name(otoa.getId()).type(GraphQLTypeReference.typeRef(logicalTypeNameSanitized(logicalTypeNameOfField)));
                            objectTypeBuilder.field(fieldBuilder);

                            break;

                        case VALUE:

                            // todo: map ...
                            GraphQLFieldDefinition.Builder valueBuilder = newFieldDefinition().name(otoa.getId()).type(Scalars.GraphQLString);
                            objectTypeBuilder.field(valueBuilder);

                            break;

                    }
                });


        // create meta type

        BeanSort objectSpecificationBeanSort = objectSpecification.getBeanSort();
        String metaTypeName = logicalTypeNameSanitized + "__DomainObject_meta";
        GraphQLObjectType.Builder metaTypeBuilder = newObject().name(metaTypeName);
        GraphQLFieldDefinition idField = newFieldDefinition().name("id").type(Scalars.GraphQLString).build();
        metaTypeBuilder.field(idField);
        GraphQLFieldDefinition logicalTypeNameField = newFieldDefinition().name("logicalTypeName").type(Scalars.GraphQLString).build();
        metaTypeBuilder.field(logicalTypeNameField);

        GraphQLFieldDefinition versionField = null;
        if (objectSpecificationBeanSort == BeanSort.ENTITY) {
            versionField = newFieldDefinition().name("version").type(Scalars.GraphQLString).build();
            metaTypeBuilder.field(versionField);
        }
        GraphQLObjectType metaType = metaTypeBuilder.build();
        graphQLObjectTypes.add(metaType);

        GraphQLFieldDefinition gql_meta = newFieldDefinition().name("_gql_meta").type(metaType).build();
        objectTypeBuilder.field(gql_meta);
        GraphQLObjectType graphQLObjectType = objectTypeBuilder.build();
        graphQLObjectTypes.add(graphQLObjectType);

        objectSpecification.streamProperties(MixedIn.INCLUDED)
                .forEach(otoa -> {

                    ObjectSpecification fieldObjectSpecification = otoa.getElementType();
                    BeanSort beanSort = fieldObjectSpecification.getBeanSort();
                    switch (beanSort) {

                        case VIEW_MODEL:
                        case ENTITY:

                            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLObjectType, otoa.getId()), new DataFetcher<Object>() {

                                @Override
                                public Object get(DataFetchingEnvironment environment) throws Exception {

                                    Object domainObjectInstance = environment.getSource();

                                    Class<?> domainObjectInstanceClass = domainObjectInstance.getClass();
                                    ObjectSpecification specification = specificationLoader.loadSpecification(domainObjectInstanceClass);

                                    ManagedObject owner = ManagedObject.of(specification, domainObjectInstance);

                                    ManagedObject managedObject = otoa.get(owner);

                                    return managedObject.getPojo();

                                }

                            });


                            break;

                        case VALUE:



                            break;

                    }
                });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLObjectType, gql_meta), new DataFetcher<Object>(){
            @Override
            public Object get(DataFetchingEnvironment environment) throws Exception {

                Bookmark bookmark = bookmarkService.bookmarkFor(environment.getSource()).orElse(null);
                if (bookmark == null) return null; //TODO: is this correct ?
                return new GQLMeta(bookmark, bookmarkService);
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, idField), new DataFetcher<Object>(){
            @Override
            public Object get(DataFetchingEnvironment environment) throws Exception {

                GQLMeta gqlMeta = environment.getSource();

                return gqlMeta.id();
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, logicalTypeNameField), new DataFetcher<Object>(){
            @Override
            public Object get(DataFetchingEnvironment environment) throws Exception {

                GQLMeta gqlMeta = environment.getSource();

                return gqlMeta.logicalTypeName();
            }
        });

        if (objectSpecificationBeanSort == BeanSort.ENTITY){
            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, versionField), new DataFetcher<Object>(){
                @Override
                public Object get(DataFetchingEnvironment environment) throws Exception {

                    GQLMeta gqlMeta = environment.getSource();

                    return gqlMeta.version();
                }
            });

        }

        return;
    }

    public static String logicalTypeNameSanitized(final String logicalTypeName) {
        return logicalTypeName.replace('.', '_');
    }
}
