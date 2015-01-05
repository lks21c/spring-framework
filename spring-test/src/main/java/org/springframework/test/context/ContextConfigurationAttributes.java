/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * {@code ContextConfigurationAttributes}는
 * {@link ContextConfiguration @ContextConfiguration}를 통해 정의된 context configuration 어트리뷰트를 감쌈.
 *
 * @author Sam Brannen
 * @since 3.1
 * @see ContextConfiguration
 * @see SmartContextLoader#processContextConfiguration(ContextConfigurationAttributes)
 * @see MergedContextConfiguration
 */
public class ContextConfigurationAttributes {

	private static final Log logger = LogFactory.getLog(ContextConfigurationAttributes.class);

	private final Class<?> declaringClass;

	private Class<?>[] classes;

	private String[] locations;

	private final boolean inheritLocations;

	private final Class<? extends ApplicationContextInitializer<? extends ConfigurableApplicationContext>>[] initializers;

	private final boolean inheritInitializers;

	private final String name;

	private final Class<? extends ContextLoader> contextLoaderClass;


	/**
	 *
	 * 주어진 {@link ContextConfiguration @ContextConfiguration} 어노테이션과
	 * {@code @ContextConfiguration}가 선언된 {@linkplain Class test class}를 위한 {@link ContextConfigurationAttributes} 인스턴스 생성.
	 * @param declaringClass {@code @ContextConfiguration}를 선언한 테스트 클래스
	 * @param contextConfiguration 어떤 어트리뷰트를 얻을지 정한 어노테이션
	 */
	public ContextConfigurationAttributes(Class<?> declaringClass, ContextConfiguration contextConfiguration) {
		this(declaringClass, resolveLocations(declaringClass, contextConfiguration), contextConfiguration.classes(),
				contextConfiguration.inheritLocations(), contextConfiguration.initializers(),
				contextConfiguration.inheritInitializers(), contextConfiguration.name(), contextConfiguration.loader());
	}

	/**
	 *
	 * ContextConfigurationAttributes 생성자
	 * @param declaringClass {@code @ContextConfiguration}를 선언한 테스트 클래스
	 * @param annAttrs  어떤 어트리뷰트를 얻을지 정한 어노테이션
	 */
	@SuppressWarnings("unchecked")
	public ContextConfigurationAttributes(Class<?> declaringClass, AnnotationAttributes annAttrs) {
		this(declaringClass,
				resolveLocations(declaringClass, annAttrs.getStringArray("locations"), annAttrs.getStringArray("value")),
				annAttrs.getClassArray("classes"), annAttrs.getBoolean("inheritLocations"),
				(Class<? extends ApplicationContextInitializer<? extends ConfigurableApplicationContext>>[]) annAttrs.getClassArray("initializers"),
				annAttrs.getBoolean("inheritInitializers"), annAttrs.getString("name"),
				(Class<? extends ContextLoader>) annAttrs.getClass("loader"));
	}

	/**
	 * ContextConfigurationAttributes 생성자
	 * @param declaringClass {@code @ContextConfiguration}를 선언한 테스트 클래스
	 * @param locations {@code @ContextConfiguration}를 통해 정의된 리소스 위치
	 * @param classes {@code @ContextConfiguration}를 통해 정의된 어노테이션 클래스
	 * @param inheritLocations {@code @ContextConfiguration}를 통해 정의된 {@code inheritLocations} 플래그
	 * @param contextLoaderClass {@code @ContextConfiguration}를 통해 정의된 {@code ContextLoader} 클래스
	 * @throws IllegalArgumentException 만약 {@code declaringClass} 또는 {@code contextLoaderClass} 가 {@code null}일 경우
	 * @deprecated 스프링 3.2 출시후, {@link #ContextConfigurationAttributes(Class, String[], Class[], boolean, Class[], boolean, String, Class)}를 쓸 것
	 * instead
	 */
	@Deprecated
	public ContextConfigurationAttributes(Class<?> declaringClass, String[] locations, Class<?>[] classes,
			boolean inheritLocations, Class<? extends ContextLoader> contextLoaderClass) {

		this(declaringClass, locations, classes, inheritLocations, null, true, null, contextLoaderClass);
	}

	/**
	 * ContextConfigurationAttributes 생성자
	 * @param declaringClass the test class that declared {@code @ContextConfiguration}
	 * @param locations {@code @ContextConfiguration}를 통해 정의된 리소스 위치
	 * @param classes {@code @ContextConfiguration}를 통해 정의된 어노테이션 클래스
	 * @param inheritLocations {@code @ContextConfiguration}를 통해 정의된 {@code inheritLocations} 플래그
	 * @param initializers {@code @ContextConfiguration}를 통해 정의된 context initializers
	 * @param inheritInitializers {@code @ContextConfiguration}를 통해 정의된 {@code inheritInitializers} 플래그
	 * @param contextLoaderClass {@code @ContextConfiguration}를 통해 정의된 {@code ContextLoader} 클래스
	 * @throws IllegalArgumentException 만약 {@code declaringClass} 또는 {@code contextLoaderClass} 가 {@code null}일 경우
	 */
	public ContextConfigurationAttributes(
			Class<?> declaringClass, String[] locations, Class<?>[] classes, boolean inheritLocations,
			Class<? extends ApplicationContextInitializer<? extends ConfigurableApplicationContext>>[] initializers,
			boolean inheritInitializers, Class<? extends ContextLoader> contextLoaderClass) {

		this(declaringClass, locations, classes, inheritLocations, initializers, inheritInitializers, null,
				contextLoaderClass);
	}

	/**
	 * ContextConfigurationAttributes 생성자
	 * @param declaringClass {@code @ContextConfiguration}를 선언한 테스트 클래스
	 * @param locations {@code @ContextConfiguration}를 통해 정의된 리소스 위치
	 * @param classes {@code @ContextConfiguration}를 통해 정의된 어노테이션 클래스
	 * @param inheritLocations {@code @ContextConfiguration}를 통해 정의된 {@code inheritLocations} 플래그
	 * @param initializers the context initializers declared via {@code @ContextConfiguration}
	 * @param inheritInitializers the {@code inheritInitializers} flag declared via {@code @ContextConfiguration}
	 * @param name the name of level in the context hierarchy, or {@code null} if not applicable
	 * @param contextLoaderClass {@code @ContextConfiguration}를 통해 정의된 {@code ContextLoader} 클래스
	 * @throws IllegalArgumentException 만약 {@code declaringClass} 또는 {@code contextLoaderClass} 가 {@code null}일 경우
	 */
	public ContextConfigurationAttributes(
			Class<?> declaringClass, String[] locations, Class<?>[] classes, boolean inheritLocations,
			Class<? extends ApplicationContextInitializer<? extends ConfigurableApplicationContext>>[] initializers,
			boolean inheritInitializers, String name, Class<? extends ContextLoader> contextLoaderClass) {

		Assert.notNull(declaringClass, "declaringClass must not be null");
		Assert.notNull(contextLoaderClass, "contextLoaderClass must not be null");

		if (!ObjectUtils.isEmpty(locations) && !ObjectUtils.isEmpty(classes) && logger.isDebugEnabled()) {
			logger.debug(String.format(
					"Test class [%s] has been configured with @ContextConfiguration's 'locations' (or 'value') %s " +
							"and 'classes' %s attributes. Most SmartContextLoader implementations support " +
							"only one declaration of resources per @ContextConfiguration annotation.",
					declaringClass.getName(), ObjectUtils.nullSafeToString(locations),
					ObjectUtils.nullSafeToString(classes)));
		}

		this.declaringClass = declaringClass;
		this.locations = locations;
		this.classes = classes;
		this.inheritLocations = inheritLocations;
		this.initializers = initializers;
		this.inheritInitializers = inheritInitializers;
		this.name = (StringUtils.hasText(name) ? name : null);
		this.contextLoaderClass = contextLoaderClass;
	}


	/**
	 * 주어진 {@link ContextConfiguration}의 {@link ContextConfiguration#locations() locations}
	 * 와 {@link ContextConfiguration#value() value} 어트리뷰트로부터 리소스 위치를 해석함.
	 *
	 * @throws IllegalStateException 만약 두 locations와 value 어트리뷰트가 선언되었을 시
	 */
	private static String[] resolveLocations(Class<?> declaringClass, ContextConfiguration contextConfiguration) {
		return resolveLocations(declaringClass, contextConfiguration.locations(), contextConfiguration.value());
	}

	/**
	 * 주어진 {@link ContextConfiguration}의 {@link ContextConfiguration#locations() locations}
	 * 와 {@link ContextConfiguration#value() value} 어트리뷰트로부터 리소스 위치를 해석함.
	 *
	 * @throws IllegalStateException 만약 두 locations와 value 어트리뷰트가 선언되었을 시
	 */
	private static String[] resolveLocations(Class<?> declaringClass, String[] locations, String[] value) {
		Assert.notNull(declaringClass, "declaringClass must not be null");
		if (!ObjectUtils.isEmpty(value) && !ObjectUtils.isEmpty(locations)) {
			throw new IllegalStateException(String.format("Test class [%s] has been configured with " +
							"@ContextConfiguration's 'value' %s and 'locations' %s attributes. Only one declaration " +
							"of resource locations is permitted per @ContextConfiguration annotation.",
					declaringClass.getName(), ObjectUtils.nullSafeToString(value), ObjectUtils.nullSafeToString(locations)));
		}
		else if (!ObjectUtils.isEmpty(value)) {
			locations = value;
		}
		return locations;
	}


	/**
	 * {@link ContextConfiguration @ContextConfiguration} 어노테이션을 선언한 {@linkplain Class class}를 가져옴.
	 * @return 선언한 클래스(절대 {@code null}이면 안됨)
	 */
	public Class<?> getDeclaringClass() {
		return this.declaringClass;
	}

	/**
	 * <em>processed</em> 어노테이션 클래스를 set함, 효과적으로 {@link ContextConfiguration @ContextConfiguration}를
	 * 통해 선언한 원래 값을 overriding 함.
	 * @see #getClasses()
	 */
	public void setClasses(Class<?>... classes) {
		this.classes = classes;
	}

	/**
	 * {@link ContextConfiguration @ContextConfiguration}를 통해 선언된 어노테이션 클래스를 가져옴.
	 *
	 * <p>노트: 이것은 변할수 있는 프로퍼티임. 리턴값은 그러므로 <em>processed</em>값을 나타냄({@link ContextConfiguration @ContextConfiguration}를
	 * 통해 선언한 원래 값이랑 다를 수 있음).</p>
	 *
	 * @return 어노테이션 클래스;잠재적으로 {@code null} 이나 <em>empty</em>
	 * @see ContextConfiguration#classes
	 * @see #setClasses(Class[])
	 */
	public Class<?>[] getClasses() {
		return this.classes;
	}

	/**
	 *
	 * {@code ContextConfigurationAttributes} 인스턴스가 클래스 기반의 리소스를 가졌는지 정의
	 *
	 * @return {@link #getClasses() classes} 배열이 비어있지 않으면 {@code true}
	 * @see #hasResources()
	 * @see #hasLocations()
	 */
	public boolean hasClasses() {
		return !ObjectUtils.isEmpty(getClasses());
	}

	/**
	 * <em>processed</em> 리소스 위치 set, 효과적으로 {@link ContextConfiguration @ContextConfiguration}를
	 * 통해 선언한 원래 값을 overriding 함.
	 *
	 * @see #getLocations()
	 */
	public void setLocations(String... locations) {
		this.locations = locations;
	}

	/**
	 *
	 *
	 * Get the resource locations that were declared via
	 * {@link ContextConfiguration @ContextConfiguration}.
	 * <p>Note: this is a mutable property. The returned value may therefore
	 * represent a <em>processed</em> value that does not match the original value
	 * declared via {@link ContextConfiguration @ContextConfiguration}.
	 * @return the resource locations; potentially {@code null} or <em>empty</em>
	 * @see ContextConfiguration#value
	 * @see ContextConfiguration#locations
	 * @see #setLocations(String[])
	 */
	public String[] getLocations() {
		return this.locations;
	}

	/**
	 * Determine if this {@code ContextConfigurationAttributes} instance has
	 * path-based resource locations.
	 * @return {@code true} if the {@link #getLocations() locations} array is not empty
	 * @see #hasResources()
	 * @see #hasClasses()
	 */
	public boolean hasLocations() {
		return !ObjectUtils.isEmpty(getLocations());
	}

	/**
	 * Determine if this {@code ContextConfigurationAttributes} instance has
	 * either path-based resource locations or class-based resources.
	 * @return {@code true} if either the {@link #getLocations() locations}
	 * or the {@link #getClasses() classes} array is not empty
	 * @see #hasLocations()
	 * @see #hasClasses()
	 */
	public boolean hasResources() {
		return (hasLocations() || hasClasses());
	}

	/**
	 * Get the {@code inheritLocations} flag that was declared via
	 * {@link ContextConfiguration @ContextConfiguration}.
	 * @return the {@code inheritLocations} flag
	 * @see ContextConfiguration#inheritLocations
	 */
	public boolean isInheritLocations() {
		return this.inheritLocations;
	}

	/**
	 * Get the {@code ApplicationContextInitializer} classes that were declared via
	 * {@link ContextConfiguration @ContextConfiguration}.
	 * @return the {@code ApplicationContextInitializer} classes
	 * @since 3.2
	 */
	public Class<? extends ApplicationContextInitializer<? extends ConfigurableApplicationContext>>[] getInitializers() {
		return this.initializers;
	}

	/**
	 * Get the {@code inheritInitializers} flag that was declared via
	 * {@link ContextConfiguration @ContextConfiguration}.
	 * @return the {@code inheritInitializers} flag
	 * @since 3.2
	 */
	public boolean isInheritInitializers() {
		return this.inheritInitializers;
	}

	/**
	 * Get the name of the context hierarchy level that was declared via
	 * {@link ContextConfiguration @ContextConfiguration}.
	 * @return the name of the context hierarchy level or {@code null} if not applicable
	 * @see ContextConfiguration#name()
	 * @since 3.2.2
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the {@code ContextLoader} class that was declared via
	 * {@link ContextConfiguration @ContextConfiguration}.
	 * @return the {@code ContextLoader} class
	 * @see ContextConfiguration#loader
	 */
	public Class<? extends ContextLoader> getContextLoaderClass() {
		return this.contextLoaderClass;
	}


	/**
	 * Determine if the supplied object is equal to this
	 * {@code ContextConfigurationAttributes} instance by comparing both object's
	 * {@linkplain #getDeclaringClass() declaring class},
	 * {@linkplain #getLocations() locations},
	 * {@linkplain #getClasses() annotated classes},
	 * {@linkplain #isInheritLocations() inheritLocations flag},
	 * {@linkplain #getInitializers() context initializer classes},
	 * {@linkplain #isInheritInitializers() inheritInitializers flag}, and the
	 * {@link #getContextLoaderClass() ContextLoader class}.
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ContextConfigurationAttributes)) {
			return false;
		}
		ContextConfigurationAttributes otherAttr = (ContextConfigurationAttributes) other;
		return (ObjectUtils.nullSafeEquals(this.declaringClass, otherAttr.declaringClass) &&
				Arrays.equals(this.classes, otherAttr.classes)) &&
				Arrays.equals(this.locations, otherAttr.locations) &&
				this.inheritLocations == otherAttr.inheritLocations &&
				Arrays.equals(this.initializers, otherAttr.initializers) &&
				this.inheritInitializers == otherAttr.inheritInitializers &&
				ObjectUtils.nullSafeEquals(this.name, otherAttr.name) &&
				ObjectUtils.nullSafeEquals(this.contextLoaderClass, otherAttr.contextLoaderClass);
	}

	/**
	 * {@linkplain #getName() name}를 제외한 이 {@code ContextConfigurationAttributes} 인스턴스의
	 * 모든 프로퍼티의 유니크 해시 코드를 생성함.
	 */
	@Override
	public int hashCode() {
		int result = this.declaringClass.hashCode();
		result = 31 * result + Arrays.hashCode(this.classes);
		result = 31 * result + Arrays.hashCode(this.locations);
		result = 31 * result + Arrays.hashCode(this.initializers);
		return result;
	}

	/**
	 * context configuration attributes와 정의된 클래스를 문자열로 제공함.
	 */
	@Override
	public String toString() {
		return new ToStringCreator(this)
				.append("declaringClass", this.declaringClass.getName())
				.append("classes", ObjectUtils.nullSafeToString(this.classes))
				.append("locations", ObjectUtils.nullSafeToString(this.locations))
				.append("inheritLocations", this.inheritLocations)
				.append("initializers", ObjectUtils.nullSafeToString(this.initializers))
				.append("inheritInitializers", this.inheritInitializers)
				.append("name", this.name)
				.append("contextLoaderClass", this.contextLoaderClass.getName())
				.toString();
	}

}
