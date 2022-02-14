package org.apache.isis.viewer.graphql.viewer.source;

import graphql.Scalars;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

public class TypeMapper {

    public static GraphQLType typeFor(final Class c){
        if (c.equals(Integer.class)){
            return Scalars.GraphQLInt;
        }
        return Scalars.GraphQLString;
    }

    public static GraphQLInputType inputTypeFor(final ObjectActionParameter objectActionParameter){
        ObjectSpecification elementType = objectActionParameter.getElementType();
        switch (elementType.getBeanSort()) {
            case VALUE:
                // TODO
            case COLLECTION:
                // TODO
            case ENTITY:
                // TODO
            case VIEW_MODEL:
                // TODO
            default:
                // for now
                return Scalars.GraphQLString;
        }

    }

    public static GraphQLType typeForObjectAction(final ObjectAction objectAction){
        ObjectSpecification objectSpecification = objectAction.getReturnType();
        switch (objectSpecification.getBeanSort()){

            case COLLECTION:

                TypeOfFacet facet = objectAction.getFacet(TypeOfFacet.class);
                ObjectSpecification objectSpecificationForElementWhenCollection = facet.valueSpec();
                return GraphQLList.list(outputTypeFor(objectSpecificationForElementWhenCollection));

            case VALUE:
            case ENTITY:
            case VIEW_MODEL:
            default:
                return outputTypeFor(objectSpecification);

        }
    }

    public static GraphQLType outputTypeFor(final ObjectSpecification objectSpecification){

        switch (objectSpecification.getBeanSort()){
            case ABSTRACT:
            case ENTITY:
            case VIEW_MODEL:
                return GraphQLTypeReference.typeRef(Utils.logicalTypeNameSanitized(objectSpecification.getLogicalTypeName()));

            case VALUE:
                return typeFor(objectSpecification.getCorrespondingClass());

            case COLLECTION:
                // should be noop
                return null;

            default:
                // for now
                return Scalars.GraphQLString;
        }
    }

}
