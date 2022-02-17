package org.apache.isis.viewer.graphql.viewer.source;

import graphql.Scalars;
import graphql.schema.*;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.apache.isis.viewer.graphql.viewer.source.Utils.metaTypeName;
import static org.apache.isis.viewer.graphql.viewer.source.Utils.mutatorsTypeName;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ObjectTypeFactory {

    private final BookmarkService bookmarkService;
    private final SpecificationLoader specificationLoader;

    private static GraphQLFieldDefinition idField = newFieldDefinition().name("id").type(Scalars.GraphQLString).build();
    private static GraphQLFieldDefinition logicalTypeNameField = newFieldDefinition().name("logicalTypeName").type(Scalars.GraphQLString).build();
    private static GraphQLFieldDefinition versionField = newFieldDefinition().name("version").type(Scalars.GraphQLString).build();

    public void objectTypeFromObjectSpecification(final ObjectSpecification objectSpecification, final Set<GraphQLType> graphQLObjectTypes, final GraphQLCodeRegistry.Builder codeRegistryBuilder) {

        val logicalTypeName = objectSpecification.getLogicalTypeName();
        String logicalTypeNameSanitized = Utils.logicalTypeNameSanitized(logicalTypeName);

        GraphQLObjectType.Builder objectTypeBuilder = newObject().name(logicalTypeNameSanitized);

        // create meta field type
        BeanSort objectSpecificationBeanSort = objectSpecification.getBeanSort();
        GraphQLObjectType metaType = createAndRegisterMetaType(logicalTypeNameSanitized, objectSpecificationBeanSort, graphQLObjectTypes);

        // add meta field
        GraphQLFieldDefinition gql_meta = newFieldDefinition().name("_gql_meta").type(metaType).build();
        objectTypeBuilder.field(gql_meta);

        // add fields
        addFields(objectSpecification, objectTypeBuilder);

        // add collections
        addCollections(objectSpecification, objectTypeBuilder);

        // add actions
        addActions(logicalTypeNameSanitized, objectSpecification, objectTypeBuilder, graphQLObjectTypes);

        // build and register object type
        GraphQLObjectType graphQLObjectType = objectTypeBuilder.build();
        graphQLObjectTypes.add(graphQLObjectType);

        // create and register data fetchers
        createAndRegisterDataFetchersForMetaData(codeRegistryBuilder, objectSpecificationBeanSort, metaType, gql_meta, graphQLObjectType);
        createAndRegisterDataFetchersForField(objectSpecification, codeRegistryBuilder, graphQLObjectType);
        createAndRegisterDataFetchersForCollection(objectSpecification, codeRegistryBuilder, graphQLObjectType);

        return;
    }

    void addFields(ObjectSpecification objectSpecification, GraphQLObjectType.Builder objectTypeBuilder) {
        objectSpecification.streamProperties(MixedIn.INCLUDED)
                .forEach(otoa -> {

                    ObjectSpecification fieldObjectSpecification = otoa.getElementType();
                    BeanSort beanSort = fieldObjectSpecification.getBeanSort();
                    switch (beanSort) {

                        case VIEW_MODEL:
                        case ENTITY:

                            String logicalTypeNameOfField = fieldObjectSpecification.getLogicalTypeName();

                            GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition().name(otoa.getId()).type(GraphQLTypeReference.typeRef(Utils.logicalTypeNameSanitized(logicalTypeNameOfField)));
                            objectTypeBuilder.field(fieldBuilder);

                            break;

                        case VALUE:

                            // todo: map ...
                            GraphQLFieldDefinition.Builder valueBuilder = newFieldDefinition().name(otoa.getId()).type(Scalars.GraphQLString);
                            objectTypeBuilder.field(valueBuilder);

                            break;

                    }
                });
    }

    void createAndRegisterDataFetchersForField(ObjectSpecification objectSpecification, GraphQLCodeRegistry.Builder codeRegistryBuilder, GraphQLObjectType graphQLObjectType) {
        objectSpecification.streamProperties(MixedIn.INCLUDED)
                .forEach(otoa -> {

                    createAndRegisterDataFetcherForObjectAssociation(codeRegistryBuilder, graphQLObjectType, otoa);

                });
    }

    void addCollections(ObjectSpecification objectSpecification, GraphQLObjectType.Builder objectTypeBuilder) {

        objectSpecification.streamCollections(MixedIn.INCLUDED).forEach(otom -> {

            ObjectSpecification elementType = otom.getElementType();
            BeanSort beanSort = elementType.getBeanSort();
            switch (beanSort) {

                case VIEW_MODEL:
                case ENTITY:

                    String logicalTypeNameOfField = elementType.getLogicalTypeName();
                    GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition().name(otom.getId()).type(GraphQLList.list(GraphQLTypeReference.typeRef(Utils.logicalTypeNameSanitized(logicalTypeNameOfField))));
                    objectTypeBuilder.field(fieldBuilder);

                    break;

                case VALUE:

                    GraphQLFieldDefinition.Builder valueBuilder = newFieldDefinition().name(otom.getId()).type(GraphQLList.list(TypeMapper.typeFor(elementType.getCorrespondingClass())));
                    objectTypeBuilder.field(valueBuilder);

                    break;

            }

        });

    }

    void createAndRegisterDataFetchersForCollection(ObjectSpecification objectSpecification, GraphQLCodeRegistry.Builder codeRegistryBuilder, GraphQLObjectType graphQLObjectType) {

        objectSpecification.streamCollections(MixedIn.INCLUDED)
                .forEach(otom -> {
                    createAndRegisterDataFetcherForObjectAssociation(codeRegistryBuilder, graphQLObjectType, otom);
                });
    }

    void addActions(String logicalTypeNameSanitized, ObjectSpecification objectSpecification, GraphQLObjectType.Builder objectTypeBuilder, final Set<GraphQLType> graphQLObjectTypes) {

        String mutatorsTypeName = mutatorsTypeName(logicalTypeNameSanitized);
        GraphQLObjectType.Builder mutatorsTypeBuilder = newObject().name(mutatorsTypeName);
        final List<String> mutatorFieldNames = new ArrayList<>();

        objectSpecification.streamActions(ActionScope.PRODUCTION, MixedIn.INCLUDED)
                .forEach(objectAction -> {

                    if (objectAction.getSemantics().isSafeInNature()) {

                        String fieldName = objectAction.getId();
                        GraphQLFieldDefinition.Builder builder = newFieldDefinition()
                                .name(fieldName)
                                .type((GraphQLOutputType) TypeMapper.typeForObjectAction(objectAction));
                        if (objectAction.getParameters().isNotEmpty()) {
                            builder.arguments(objectAction.getParameters().stream()
                                    .map(objectActionParameter -> GraphQLArgument.newArgument()
                                            .name(objectActionParameter.getId())
                                            .type(TypeMapper.inputTypeFor(objectActionParameter))
                                            .build())
                                    .collect(Collectors.toList()));
                        }
                        objectTypeBuilder.field(builder);

                    } else {

                        String fieldName = objectAction.getId();
                        GraphQLFieldDefinition.Builder builder = newFieldDefinition()
                                .name(fieldName)
                                .type((GraphQLOutputType) TypeMapper.typeForObjectAction(objectAction));
                        if (objectAction.getParameters().isNotEmpty()) {
                            builder.arguments(objectAction.getParameters().stream()
                                    .map(objectActionParameter -> GraphQLArgument.newArgument()
                                            .name(objectActionParameter.getId())
                                            .type(TypeMapper.inputTypeFor(objectActionParameter))
                                            .build())
                                    .collect(Collectors.toList()));
                        }

                        mutatorsTypeBuilder.field(builder);
                        mutatorFieldNames.add(fieldName);

                    }


                });

        if (!mutatorFieldNames.isEmpty()){
            GraphQLObjectType mutatorsType = mutatorsTypeBuilder.build();
            graphQLObjectTypes.add(mutatorsType);
            objectTypeBuilder.field(newFieldDefinition().name("_gql_mutators").type(mutatorsType).build());
        }

    }

    private void createAndRegisterDataFetcherForObjectAssociation(GraphQLCodeRegistry.Builder codeRegistryBuilder, GraphQLObjectType graphQLObjectType, ObjectAssociation otom) {
        ObjectSpecification fieldObjectSpecification = otom.getElementType();
        BeanSort beanSort = fieldObjectSpecification.getBeanSort();
        switch (beanSort) {

            case VALUE: //TODO: does this work for values as well?

            case VIEW_MODEL:

            case ENTITY:

                codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLObjectType, otom.getId()), (DataFetcher<Object>) environment -> {

                    Object domainObjectInstance = environment.getSource();

                    Class<?> domainObjectInstanceClass = domainObjectInstance.getClass();
                    ObjectSpecification specification = specificationLoader.loadSpecification(domainObjectInstanceClass);

                    ManagedObject owner = ManagedObject.of(specification, domainObjectInstance);

                    ManagedObject managedObject = otom.get(owner);

                    return managedObject.getPojo();

                });


                break;

        }
    }


    GraphQLObjectType createAndRegisterMetaType(final String logicalTypeNameSanitized, final BeanSort objectSpecificationBeanSort, final Set<GraphQLType> graphQLObjectTypes) {
        String metaTypeName = metaTypeName(logicalTypeNameSanitized);
        GraphQLObjectType.Builder metaTypeBuilder = newObject().name(metaTypeName);
        metaTypeBuilder.field(idField);
        metaTypeBuilder.field(logicalTypeNameField);
        if (objectSpecificationBeanSort == BeanSort.ENTITY) {
            metaTypeBuilder.field(versionField);
        }
        GraphQLObjectType metaType = metaTypeBuilder.build();
        graphQLObjectTypes.add(metaType);
        return metaType;
    }

    GraphQLObjectType createAndRegisterMutatorsType(final String logicalTypeNameSanitized, final BeanSort objectSpecificationBeanSort, final Set<GraphQLType> graphQLObjectTypes) {
        //TODO: this is not going to work, because we need to dynamically add fields
        String mutatorsTypeName = mutatorsTypeName(logicalTypeNameSanitized);
        GraphQLObjectType.Builder mutatorsTypeBuilder = newObject().name(mutatorsTypeName);
        GraphQLObjectType mutatorsType = mutatorsTypeBuilder.build();
        graphQLObjectTypes.add(mutatorsType);
        return mutatorsType;
    }

    void createAndRegisterDataFetchersForMetaData(GraphQLCodeRegistry.Builder codeRegistryBuilder, BeanSort objectSpecificationBeanSort, GraphQLObjectType metaType, GraphQLFieldDefinition gql_meta, GraphQLObjectType graphQLObjectType) {
        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(graphQLObjectType, gql_meta), new DataFetcher<Object>() {
            @Override
            public Object get(DataFetchingEnvironment environment) throws Exception {

                Bookmark bookmark = bookmarkService.bookmarkFor(environment.getSource()).orElse(null);
                if (bookmark == null) return null; //TODO: is this correct ?
                return new GQLMeta(bookmark, bookmarkService);
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, idField), new DataFetcher<Object>() {
            @Override
            public Object get(DataFetchingEnvironment environment) throws Exception {

                GQLMeta gqlMeta = environment.getSource();

                return gqlMeta.id();
            }
        });

        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, logicalTypeNameField), new DataFetcher<Object>() {
            @Override
            public Object get(DataFetchingEnvironment environment) throws Exception {

                GQLMeta gqlMeta = environment.getSource();

                return gqlMeta.logicalTypeName();
            }
        });

        if (objectSpecificationBeanSort == BeanSort.ENTITY) {
            codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(metaType, versionField), new DataFetcher<Object>() {
                @Override
                public Object get(DataFetchingEnvironment environment) throws Exception {

                    GQLMeta gqlMeta = environment.getSource();

                    return gqlMeta.version();
                }
            });

        }
    }

}
