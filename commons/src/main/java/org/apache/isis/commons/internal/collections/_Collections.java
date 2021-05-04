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

package org.apache.isis.commons.internal.collections;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._With;
import org.apache.isis.commons.internal.reflection._Generics;

import lombok.NonNull;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common Collection creation and adapting idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Collections {

    private _Collections(){}

    // -- PREDICATES

    /**
     * @param cls
     * @return whether {@code cls} implements the java.util.Collection interface
     */
    public static boolean isCollectionType(final @Nullable Class<?> cls) {
        return cls!=null ? java.util.Collection.class.isAssignableFrom(cls) : false;
    }

    public static boolean isCanType(final @Nullable Class<?> cls) {
        return cls!=null ? Can.class.isAssignableFrom(cls) : false;
    }

    /**
     * For convenience also provided in {@link _Arrays}.
     * @param cls
     * @return whether {@code cls} implements the java.util.Collection interface
     * or represents an array
     */
    public static boolean isCollectionOrArrayType(final Class<?> cls) {
        return _Collections.isCollectionType(cls) || _Arrays.isArrayType(cls);
    }

    /**
     * @param cls
     * @return whether {@code cls} implements the java.util.Collection interface
     * or represents an array or is of type {@link Can}
     */
    public static boolean isCollectionOrArrayOrCanType(final Class<?> cls) {
        return _Collections.isCollectionType(cls) 
                || _Arrays.isArrayType(cls)
                || Can.class.isAssignableFrom(cls);
    }

    // -- COLLECTION UNMODIFIABLE ADAPTERS (FOR LIST)

    /**
     * Adapts the {@code list} as unmodifiable collection.
     * Same as {@link Collections#unmodifiableCollection(Collection)}.
     *
     * @param list
     * @return null if {@code list} is null
     */
    public static <T> Collection<T> asUnmodifiableCollection(final @Nullable List<T> list) {
        if(list==null) {
            return null;
        }
        return Collections.unmodifiableCollection(list);
    }

    /**
     * Adapts the {@code list} as unmodifiable list.
     * Same as {@link Collections#unmodifiableList(List)}.
     *
     * @param list
     */
    public static <T> List<T> asUnmodifiableList(final @Nullable List<T> list) {
        if(list==null) {
            return null;
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Preserves order, adapts the {@code list} as Set.<br/><br/>
     *
     * Any duplicate elements of the list will not be added to the set.
     * An element e1 is a duplicate of e2 if {@code e1.equals(e2) == true}.
     *
     * @param list
     * @return null if {@code list} is null
     */
    public static <T> Set<T> asUnmodifiableSet(final @Nullable List<T> list) {
        if(list==null) {
            return null;
        }
        return Collections.unmodifiableSet(
                (Set<T>)
                list.stream()
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    /**
     * Preserves order, adapts the {@code list} as SortedSet.<br/><br/>
     *
     * Any duplicate elements of the list will not be added to the set.
     * An element e1 is a duplicate of e2 if {@code e1.equals(e2) == true}.
     *
     * @param list
     * @return null if {@code list} is null
     */
    public static <T> SortedSet<T> asUnmodifiableSortedSet(final @Nullable List<T> list) {
        if(list==null) {
            return null;
        }
        return _Collections_SortedSetOfList.of(list);
    }

    // -- COMMON COLLECTORS

    public static <T> Collector<T, ?, HashSet<T>> toHashSet() {
        return Collectors.toCollection(HashSet::new);
    }

    public static <T> Collector<T, ?, ArrayList<T>> toArrayList() {
        return Collectors.toCollection(ArrayList::new);
    }

    // -- STREAM TO UMODIFIABLE COLLECTION COLLECTORS

    /**
     * @return a collector that collects elements of a stream into an unmodifiable List
     */
    public static <T> Collector<T, List<T>, List<T>> toUnmodifiableList() {
        return new _Collections_Collector<>(ArrayList::new, Collections::unmodifiableList);
    }

    /**
     * @return a collector that collects elements of a stream into an unmodifiable Set
     */
    public static <T> Collector<T, Set<T>, Set<T>> toUnmodifiableSet() {
        return new _Collections_Collector<>(HashSet::new, Collections::unmodifiableSet);
    }

    /**
     * @return a collector that collects elements of a stream into an unmodifiable SortedSet
     */
    public static <T> Collector<T, SortedSet<T>, SortedSet<T>> toUnmodifiableSortedSet() {
        return new _Collections_Collector<>(TreeSet::new, Collections::unmodifiableSortedSet);
    }

    /**
     * @return a collector that collects elements of a stream into an unmodifiable Collection
     */
    public static <T> Collector<T, Collection<T>, Collection<T>> toUnmodifiableCollection() {
        return new _Collections_Collector<>(ArrayList::new, Collections::unmodifiableCollection);
    }

    /**
     * @return a collector that collects elements of a stream into an unmodifiable SortedSet
     */
    public static <T> Collector<T, SortedSet<T>, SortedSet<T>> toUnmodifiableSortedSet(
            @Nullable Comparator<T> comparator) {

        if(comparator==null) {
            return toUnmodifiableSortedSet();
        }
        return new _Collections_Collector<>(()->new TreeSet<>(comparator), Collections::unmodifiableSortedSet);
    }

    /**
     * @return a collector that collects elements of a stream into an unmodifiable
     * List, Set, SortedSet or Collection.
     * @throws IllegalArgumentException if {@code typeOfCollection} is not one of
     * List, Set, SortedSet or Collection.
     */
    public static <T> Collector<T, ?, ? extends Collection<T>> toUnmodifiableOfType(Class<?> typeOfCollection) {

        _With.requires(typeOfCollection, "typeOfCollection");

        if(SortedSet.class.equals(typeOfCollection)) {
            return toUnmodifiableSortedSet();
        }

        if(Set.class.equals(typeOfCollection)) {
            return toUnmodifiableSet();
        }

        if(List.class.equals(typeOfCollection)) {
            return toUnmodifiableList();
        }

        if(Collection.class.equals(typeOfCollection)) {
            return toUnmodifiableCollection();
        }

        throw new IllegalArgumentException(
                String.format("Can not collect into %s. Only List, Set, SortedSet and Collection are supported.",
                        typeOfCollection.getClass().getName()));
    }

    // -- COLLECT FROM ITERABLE

    /*
     * package private utility for a slightly heap pollution reduced collection,
     * if the iterable is a collection and we know the size of the result in advance
     *
     * @param iterable
     * @param factory
     * @param elementCollector
     * @return
     */
    static <T, R> R collectFromIterable(
            @Nullable Iterable<T> iterable,
            Function<Collection<T>, R> factory,
            Supplier<Collector<T, ?, R>> elementCollector) {

        if(iterable==null) {
            return factory.apply(Collections.emptyList());
        }
        if(iterable instanceof Collection) {
            return factory.apply((Collection<T>) iterable);
        }
        return _NullSafe.stream(iterable)
                .collect(elementCollector.get());
    }

    // -- ELEMENT TYPE INFERENCE

    
    public static Optional<Class<?>> inferElementType(final @NonNull Parameter param) {
        final Class<?> parameterType = param.getType();
        final Class<?> declaringType = param.getDeclaringExecutable().getDeclaringClass();
        final Type genericParameterType = param.getParameterizedType();
        
        //TODO use _Generics instead
        return inferElementType(parameterType, genericParameterType);
    }
    
    public static Optional<Class<?>> inferElementType(final @NonNull Method method) {
        
        val returnType = method.getReturnType();
        
        if (_Collections.isCollectionType(returnType) 
                || _Collections.isCanType(returnType)) {
            
            return _Generics.streamGenericTypeArgumentsOfMethodReturnType(method)
                    .findFirst();
        }

        return Optional.empty();
    }
    
    
    /**
     * If the {@code collectionType} represents a collection then returns returns the inferred element type of the
     * specified {@code genericType}
     * @param collectionType
     * @param genericType as associated with {@code collectionType} (as available for fields or method parameters)
     * @return inferred type or null if inference fails
     */
    private static Optional<Class<?>> inferElementType(
            final @Nullable Class<?> collectionType,
            final @Nullable Type genericType) {

        if(collectionType == null || genericType==null) {
            return Optional.empty();
        }

        if(!isCollectionType(collectionType) && !isCanType(collectionType)) {
            return Optional.empty();
        }
        
        if(genericType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) genericType;
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if(actualTypeArguments.length == 1) {
                // handle e.g. List<Sometype>
                final Type actualTypeArgument = actualTypeArguments[0];
                if(actualTypeArgument instanceof Class) {
                    return Optional.of((Class<?>) actualTypeArgument);
                }
                // also handle e.g. List<Sometype<T>>
                if(actualTypeArgument instanceof ParameterizedType) {
                    final Type innerParameterizedType = ((ParameterizedType) actualTypeArgument).getRawType();
                    if(innerParameterizedType instanceof Class) {
                        return Optional.of((Class<?>) innerParameterizedType);
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * @param collectionType
     * @param genericType
     * @return optionally the inferred element type, 
     * based on whether parameter is a (collection or array) and has an infer-able element type
     */
    public static Optional<Class<?>> inferElementTypeFromArrayOrCollection(
            final @Nullable Class<?> collectionType,
            final @Nullable Type genericType) {

        val fromArray = _Arrays.inferComponentType(collectionType);
        if(fromArray.isPresent()) {
            return fromArray;
        }
        return _Collections.inferElementType(collectionType, genericType);
    }

    /**
     * If the {@code field} represents a collection then returns the inferred element type for this collection (if any).
     *
     * @param field
     * @return inferred type or null if inference fails
     */
    public static Optional<Class<?>> inferElementType(final @Nullable Field field) {
        if(field==null) {
            return null;
        }
        return inferElementType(field.getType(), field.getGenericType());
    }

    // -- TO STRING

    public static String toStringJoining(
            final @Nullable Collection<?> collection, 
            final @NonNull String delimiter) {
        return _NullSafe.stream(collection)
                .map(x->""+x)
                .collect(Collectors.joining(delimiter));
    }

    public static String toStringJoiningNewLine(@Nullable Collection<?> collection) {
        return toStringJoining(collection, "\n");
    }



    

}
