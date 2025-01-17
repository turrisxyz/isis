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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Optional;

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.validator.StringValidator;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.InputFragment;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.NonNull;
import lombok.val;

/**
 * Adapter for {@link PanelAbstract panel}s that use a {@link ScalarModel} as
 * their backing model.
 * <p>
 * Supports the concept of being
 * COMPACT (eg within a table) or
 * REGULAR (eg within a form).
 * <p>
 * This implementation is for panels that use a textfield/text area.
 */
public abstract class ScalarPanelTextFieldAbstract<T>
extends ScalarPanelFormFieldAbstract<T> {

    private static final long serialVersionUID = 1L;

    private AbstractTextComponent<T> formField;

    protected ScalarPanelTextFieldAbstract(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> type) {
        super(id, scalarModel, type);
    }

    // -- CONVERSION

    protected final IConverter<T> getConverter(final ScalarModel scalarModel) {
        return getConverter(scalarModel.getMetaModel(), scalarModel.isEditMode()
                ? ScalarRepresentation.EDITING
                : ScalarRepresentation.VIEWING);
    }

    /**
     * Converter that is used for the either regular (editing) or compact (HTML) view of the panel,
     * based on argument {@code scalarRepresentation}.
     */
    protected abstract IConverter<T> getConverter(
            @NonNull ObjectFeature propOrParam,
            @NonNull ScalarRepresentation scalarRepresentation);

    // --

    /**
     * TextField, with converter.
     */
    protected AbstractTextComponent<T> createTextField(final String id) {
        val converter = getConverter(scalarModel());
        return getFormatModifiers().contains(FormatModifier.MULITLINE)
                ? Wkt.textAreaWithConverter(
                        id, unwrappedModel(), type, converter)
                : Wkt.textFieldWithConverter(
                        id, unwrappedModel(), type, converter);
    }

    protected final IModel<T> unwrappedModel() {
        _Assert.assertTrue(scalarModel().getScalarTypeSpec().isAssignableFrom(type), ()->
            String.format("[%s] cannot possibly unwrap model of type %s into target type %s",
                    this.getClass().getSimpleName(),
                    scalarModel().getScalarTypeSpec().getCorrespondingClass(),
                    type));

        return scalarModel().unwrapped(type);
    }

    // --

    @Override
    protected final FormComponent<T> createFormComponent(final String id, final ScalarModel scalarModel) {
        formField = createTextField(id);
        formField.setOutputMarkupId(true);
        setFormComponentAttributes(formField);
        return formField;
    }

    @Override
    protected Optional<InputFragment> getInputFragmentType() {
        return Optional.of(getFormatModifiers().contains(FormatModifier.MULITLINE)
                ? InputFragment.TEXTAREA
                : InputFragment.TEXT);
    }

    @Override
    protected String obtainInlinePromptLinkCssIfAny() {
        return !getFormatModifiers().contains(FormatModifier.MULITLINE)
                ? super.obtainInlinePromptLinkCssIfAny()
                /* Most other components require 'form-control form-control-sm' on the owning inline prompt link.
                 * For TextArea, however, this instead appears on the TextArea itself.
                 */
                : null;
    }

    // -- CONVERSION

    @Override
    protected final String obtainOutputFormat() {
        // conversion does not affect the output format (usually HTML)
        return super.obtainOutputFormat();
    }

//    protected class ToStringConvertingModel<X> extends Model<String> {
//        private static final long serialVersionUID = 1L;
//
//        @NonNull private final IConverter<X> converter;
//
//        private ToStringConvertingModel(final @NonNull IConverter<X> converter) {
//            this.converter = converter;
//        }
//
//        @Override public String getObject() {
//            val adapter = scalarModel().getObject();
//            val value = ManagedObjects.UnwrapUtil.single(adapter);
//            final String str = value != null
//                    ? converter.convertToString(
//                            _Casts.uncheckedCast(value),
//                            getLanguageProvider().getPreferredLanguage().orElseGet(Locale::getDefault))
//                    : null;
//            return str;
//        }
//    }

    // -- HELPER

    void setFormComponentAttributes(final FormComponent<?> formComponent) {
        val scalarModel = scalarModel();
        val scalarTypeSpec = scalarModel.getScalarTypeSpec();

        if(formComponent instanceof TextArea) {
            Facets.multilineNumberOfLines(scalarTypeSpec)
            .ifPresent(numberOfLines->
                Wkt.attributeReplace(formComponent, "rows", numberOfLines));

            // in conjunction with javascript in jquery.isis.wicket.viewer.js
            // see http://stackoverflow.com/questions/4459610/set-maxlength-in-html-textarea
            //Wkt.attributeReplace(textArea, "maxlength", getMaxLengthOf(scalarModel));
        }

        val maxLenIfAny = Facets.maxLength(scalarTypeSpec);
        maxLenIfAny
        .ifPresent(maxLength->{
            Wkt.attributeReplace(formComponent, "maxlength", maxLength);
            if(type.equals(String.class)) {
                formComponent.add(StringValidator.maximumLength(maxLength));
            }
        });

        Facets.typicalLength(scalarTypeSpec, maxLenIfAny)
        .ifPresent(typicalLength->
            Wkt.attributeReplace(formComponent, "size", typicalLength));

    }

}

