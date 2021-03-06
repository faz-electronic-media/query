/*
 * Copyright (c) 2013. F.A.Z. Electronic Media GmbH
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains
 * the property of F.A.Z. Electronic Media GmbH and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to F.A.Z. Electronic Media GmbH
 * and its suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from F.A.Z. Electronic Media GmbH.
 */

package de.faz.modules.query.fields;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Stack;

/** @author Andreas Kaubisch <a.kaubisch@faz.de> */
public class FieldDefinitionGenerator {

	private final Stack<FieldDefinition> fieldStack;

	public FieldDefinitionGenerator() {
		fieldStack = new Stack<FieldDefinition>();
	}

	public <T extends Mapping> T createFieldDefinition(final Class<T> mappingClass) {
		return enhanceWithInterceptor(mappingClass, new MethodInterceptor() {
			@Override
			public Object intercept(final Object o, final Method method, final Object[] objects, final MethodProxy methodProxy)
					throws Throwable {
				String fieldName = null;
				Integer boost = null;

				final MapToField mapping = method.getAnnotation(MapToField.class);
				if (mapping != null) {
					fieldName = mapping.value();
				}

				final BoostResult boostResult = method.getAnnotation(BoostResult.class);
				if (boostResult != null) {
					boost = boostResult.value();
				}

				if (fieldName != null) {
					fieldStack.push(new FieldDefinition(fieldName, boost));
				}

				return null;
			}
		});
	}

	public <T extends Mapping> T enhanceWithInterceptor(final Class<T> enhancedClass, final Callback interceptor) {
		return enhanceWithInterceptorInternal(enhancedClass, interceptor);
	}

	private <T extends Object> T enhanceWithInterceptorInternal(final Class<T> enhancedClass) {
		return enhanceWithInterceptorInternal(enhancedClass, new MethodInterceptor() {
			@Override
			public Object intercept(final Object o, final Method method, final Object[] objects, final MethodProxy methodProxy) throws Throwable {
				return null;
			}
		});
	}

	private <T extends Object> T enhanceWithInterceptorInternal(final Class<T> enhancedClass, final Callback interceptor) {
		final Enhancer enhancer = new Enhancer();
		enhancer.setCallback(interceptor);
		enhancer.setSuperclass(enhancedClass);
		final Class<?>[] argumentTypes = getConstructorArgumentsFromClass(enhancedClass);
		T proxiedObject;
		if (argumentTypes == null) {
			proxiedObject = (T) enhancer.create();
		} else {
			proxiedObject = (T) enhancer.create(argumentTypes, createInstances(argumentTypes));
		}
		return proxiedObject;
	}

	@Nonnull
	private Object[] createInstances(@Nonnull final Class<?>[] clazzes) {
		final Object[] objects = new Object[clazzes.length];
		for (int i = 0; i < clazzes.length; i++) {
			objects[i] = enhanceWithInterceptorInternal(clazzes[i]);
		}

		return objects;
	}

	public String getFieldNameOf(final Object fieldMethod) {
		String fieldName = null;
		if (isNotEmpty()) {
			final FieldDefinition definition = pop();
			fieldName = definition.name.toString();
		}

		return fieldName;
	}

	public boolean isEmpty() {
		return fieldStack.isEmpty();
	}

	public boolean isNotEmpty() {
		return !isEmpty();
	}

	public FieldDefinition pop() {
		return fieldStack.pop();
	}

	private Class<?>[] getConstructorArgumentsFromClass(final Class<?> cls) {
		final Constructor<?>[] constructors = cls.getConstructors();
		if (constructors != null && constructors.length > 0) {
			return constructors[0].getParameterTypes();
		}
		return null;
	}

	/** @author Andreas Kaubisch <a.kaubisch@faz.de> */
	public static class FieldDefinition {
		final CharSequence name;
		final int boost;

		public FieldDefinition(final CharSequence name, final Integer boost) {
			this.name = name;
			if (boost != null) {
				this.boost = boost;
			} else {
				this.boost = 1;
			}
		}

		public CharSequence getName() {
			return name;
		}

		public FieldDefinition setName(final String name) {
			return new FieldDefinition(name, boost);
		}

		public int getBoost() {
			return boost;
		}

		public FieldDefinition setBoost(final int boost) {
			return new FieldDefinition(name, boost);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof FieldDefinition) {
				final FieldDefinition def = (FieldDefinition) obj;
				return Objects.equals(name, def.name)
					&& Objects.equals(boost, def.boost);
			}
			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, boost);
		}
	}
}

