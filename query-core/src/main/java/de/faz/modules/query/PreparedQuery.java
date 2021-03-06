/*
 * Copyright (c) 2013. F.A.Z. Electronic Media GmbH
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of F.A.Z. Electronic Media GmbH and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to F.A.Z. Electronic Media GmbH
 * and its suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from F.A.Z. Electronic Media GmbH.
 */

package de.faz.modules.query;

import de.faz.modules.query.fields.FieldDefinitionGenerator;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** @author Andreas Kaubisch <a.kaubisch@faz.de> */
public class PreparedQuery extends Query implements Cloneable {
    private final ParameterValues parameterValues;

    PreparedQuery(final FieldDefinitionGenerator generator) {
        super(generator);
        parameterValues = new ParameterValues();
    }

    public ValueItem param(final String paramName) {
        if(StringUtils.isEmpty(paramName)) {
            throw new IllegalArgumentException("You need a parameter name for a prepared parameter in a query.");
        }
        return new PreparedValue(paramName, parameterValues);
    }

    public void setParamValue(final String paramName, final CharSequence value) {
        if(StringUtils.isEmpty(paramName)) {
            throw new IllegalArgumentException("The name of the parameter is missing.");
        }
        if(StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("The name of the parameter is missing.");
        }
        parameterValues.setParameter(paramName, value);
    }

    public void reset() {
        parameterValues.clear();
    }

    private static class PreparedValue extends ValueItem {
        private final Valuable<CharSequence> parameterValue;
        private final String paramName;
        public PreparedValue(@Nonnull final String paramName, @Nonnull final Valuable<CharSequence> value) {
            this.paramName = paramName;
            this.parameterValue = value;
        }

        @Override
        public CharSequence toCharSequence() {
            CharSequence value = parameterValue.getValueOf(paramName);
            if(value == null) {
                value = "<unset parameter '" + paramName + "'>";
            }

            return value;
        }
    }

    private static class ParameterValues implements Valuable<CharSequence> {

        private final ThreadLocal<Map<String, CharSequence>> paramMap;

        public ParameterValues() {
            paramMap = new ThreadLocal<>();
        }

        @Override
        @Nullable
        public CharSequence getValueOf(final String key) {
            return paramMap.get() != null ? paramMap.get().get(key) : null;
        }

        public void setParameter(@Nonnull final String key, @Nonnull final CharSequence value) {
            Map<String, CharSequence> parameterMap = paramMap.get();
            if(parameterMap == null) {
                parameterMap = new HashMap<>();
                paramMap.set(parameterMap);
            }

            parameterMap.put(key, value);
        }

        public void addAllParameter(@Nonnull final Map<String, CharSequence> allParams) {
            paramMap.set(allParams);
        }

        public void clear() {
            paramMap.remove();
        }
    }

    private interface Valuable<T> {
        T getValueOf(final String key);
    }
}
