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

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.annotation.*;

/**
 * {@code @ContextConfiguration}은 클레스 레벨 메타데이터를 정의하여 통합 테스트를 위해 어떻게 {@link org.springframework.context.ApplicationContext
 * ApplicationContext}을 로딩하고 설정할지 정의함.
 *
 * <h3>지원하는 리소스 타입들</h3>
 *
 * <p>
 * 스프링 3.1 이전, path-based 리소스 경로(일반적으로 xml 설정 파일들)만 지원되었음.
 * 스프링 3.1에 와서, {@linkplain #loader context loaders}은 path-based <em>또는</em> class-based 리소스를 지원하게됨.
 * 스프링 4.04에 와서, {@linkplain #loader context loaders}은 path-based <em>와</em> class-based 리소스를 동시에 지원할수 있게됨.
 * 결론적으로 {@code @ContextConfiguration}은 2가지 방법 다 사용이 가능함({@link #locations} 또는 {@link #value} attribute를 통해).
 * 노트: 그러나, {@link SmartContextLoader}의 대부분 구현체들은 오직 단일 리소스 타입만 지원함.
 * 스프링 4.1에 와서, path-based resource location은 XML configuration files 또는 그루비 스크립트일수 있음(만약 그루비가
 * class path에 있으면). 물론, 3rd party 프레임워크들은 추가로 path-based 리소스 타입을 선택 할 수 있음.
 *
 * <h3>어노테이션 클래스</h3>
 *
 *
 * <p>
 * <em>어노테이션 클래스</em>는 아래의 사항들을 말함.
 *
 *  <ul>
 * <li>{@link org.springframework.context.annotation.Configuration @Configuration} 어노테이션이 붙은 클래스</li>
 * <li>component (i.e., 다음의 어노테이션이 붙은 클래스
 * {@link org.springframework.stereotype.Component @Component},
 * {@link org.springframework.stereotype.Service @Service},
 * {@link org.springframework.stereotype.Repository @Repository}, etc.)</li>
 * <li>JSR-330 을 준수하고 {@code javax.inject} 어노테이션이 붙은 클래스</li>
 * <li>{@link org.springframework.context.annotation.Bean @Bean}-methods를 포함하는 클래스</li>
 * </ul>
 *
 * <p>
 * JavaDoc에서 아래의 사항들을 참고 할 것. <br />
 * {@link org.springframework.context.annotation.Configuration @Configuration}
 * , {@link org.springframework.context.annotation.Bean @Bean}.
 *
 * <p>
 * 스프링 프레임워크 4.0 이후, 이 어노테이션은 커스텀 <em>composed annotations</em> 어노테이션을 생성하기 위한
 * <em>meta-annotation</em>로 사용될 수 있음.
 *
 * @author Sam Brannen
 * @since 2.5
 * @see ContextHierarchy
 * @see ActiveProfiles
 * @see TestPropertySource
 * @see ContextLoader
 * @see SmartContextLoader
 * @see ContextConfigurationAttributes
 * @see MergedContextConfiguration
 * @see org.springframework.context.ApplicationContext
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContextConfiguration {

	/**
	 * {@link #locations}의 Alias.
	 *
	 * <p>{@link #locations} 대신에 쓰는 것은 괜찮지만 동시에 같이 쓰지 말 것.</p>
	 *
	 * @since 3.0
	 * @see #inheritLocations
	 */
	String[] value() default {};

	/**
	 * The resource locations to use for loading an
	 * {@link org.springframework.context.ApplicationContext ApplicationContext}.
	 *
	 * <p>Check out the Javadoc for
	 * {@link org.springframework.test.context.support.AbstractContextLoader#modifyLocations
	 * AbstractContextLoader.modifyLocations()} for details on how a location
	 * will be interpreted at runtime, in particular in case of a relative
	 * path. Also, check out the documentation on
	 * {@link org.springframework.test.context.support.AbstractContextLoader#generateDefaultLocations
	 * AbstractContextLoader.generateDefaultLocations()} for details on the
	 * default locations that are going to be used if none are specified.
	 *
	 * <p>Note that the aforementioned default rules only apply for a standard
	 * {@link org.springframework.test.context.support.AbstractContextLoader
	 * AbstractContextLoader} subclass such as
	 * {@link org.springframework.test.context.support.GenericXmlContextLoader GenericXmlContextLoader} or
	 * {@link org.springframework.test.context.support.GenericGroovyXmlContextLoader GenericGroovyXmlContextLoader}
	 * which are the effective default implementations used at runtime if
	 * {@code locations} are configured. See the documentation for {@link #loader}
	 * for further details regarding default loaders.
	 *
	 * <p>This attribute may <strong>not</strong> be used in conjunction with
	 * {@link #value}, but it may be used instead of {@link #value}.
	 *
	 * @since 2.5
	 * @see #inheritLocations
	 */
	String[] locations() default {};

	/**
	 * The <em>annotated classes</em> to use for loading an
	 * {@link org.springframework.context.ApplicationContext ApplicationContext}.
	 *
	 * <p>Check out the Javadoc for
	 * {@link org.springframework.test.context.support.AnnotationConfigContextLoader#detectDefaultConfigurationClasses
	 * AnnotationConfigContextLoader.detectDefaultConfigurationClasses()} for details
	 * on how default configuration classes will be detected if no
	 * <em>annotated classes</em> are specified. See the documentation for
	 * {@link #loader} for further details regarding default loaders.
	 *
	 * @since 3.1
	 * @see org.springframework.context.annotation.Configuration
	 * @see org.springframework.test.context.support.AnnotationConfigContextLoader
	 * @see #inheritLocations
	 */
	Class<?>[] classes() default {};

	/**
	 * The application context <em>initializer classes</em> to use for initializing
	 * a {@link ConfigurableApplicationContext}.
	 *
	 * <p>The concrete {@code ConfigurableApplicationContext} type supported by each
	 * declared initializer must be compatible with the type of {@code ApplicationContext}
	 * created by the {@link SmartContextLoader} in use.
	 *
	 * <p>{@code SmartContextLoader} implementations typically detect whether
	 * Spring's {@link org.springframework.core.Ordered Ordered} interface has been
	 * implemented or if the @{@link org.springframework.core.annotation.Order Order}
	 * annotation is present and sort instances accordingly prior to invoking them.
	 *
	 * @since 3.2
	 * @see org.springframework.context.ApplicationContextInitializer
	 * @see org.springframework.context.ConfigurableApplicationContext
	 * @see #inheritInitializers
	 * @see #loader
	 */
	Class<? extends ApplicationContextInitializer<? extends ConfigurableApplicationContext>>[] initializers() default {};

	/**
	 * Whether or not {@link #locations resource locations} or <em>annotated
	 * classes</em> from test superclasses should be <em>inherited</em>.
	 *
	 * <p>The default value is {@code true}. This means that an annotated
	 * class will <em>inherit</em> the resource locations or annotated classes
	 * defined by test superclasses. Specifically, the resource locations or
	 * annotated classes for a given test class will be appended to the list of
	 * resource locations or annotated classes defined by test superclasses.
	 * Thus, subclasses have the option of <em>extending</em> the list of resource
	 * locations or annotated classes.
	 *
	 * <p>If {@code inheritLocations} is set to {@code false}, the
	 * resource locations or annotated classes for the annotated class
	 * will <em>shadow</em> and effectively replace any resource locations
	 * or annotated classes defined by superclasses.
	 *
	 * <p>In the following example that uses path-based resource locations, the
	 * {@link org.springframework.context.ApplicationContext ApplicationContext}
	 * for {@code ExtendedTest} will be loaded from
	 * {@code "base-context.xml"} <strong>and</strong>
	 * {@code "extended-context.xml"}, in that order. Beans defined in
	 * {@code "extended-context.xml"} may therefore override those defined
	 * in {@code "base-context.xml"}.
	 * <pre class="code">
	 * &#064;ContextConfiguration("base-context.xml")
	 * public class BaseTest {
	 *     // ...
	 * }
	 *
	 * &#064;ContextConfiguration("extended-context.xml")
	 * public class ExtendedTest extends BaseTest {
	 *     // ...
	 * }
	 * </pre>
	 *
	 * <p>Similarly, in the following example that uses annotated
	 * classes, the
	 * {@link org.springframework.context.ApplicationContext ApplicationContext}
	 * for {@code ExtendedTest} will be loaded from the
	 * {@code BaseConfig} <strong>and</strong> {@code ExtendedConfig}
	 * configuration classes, in that order. Beans defined in
	 * {@code ExtendedConfig} may therefore override those defined in
	 * {@code BaseConfig}.
	 * <pre class="code">
	 * &#064;ContextConfiguration(classes=BaseConfig.class)
	 * public class BaseTest {
	 *     // ...
	 * }
	 *
	 * &#064;ContextConfiguration(classes=ExtendedConfig.class)
	 * public class ExtendedTest extends BaseTest {
	 *     // ...
	 * }
	 * </pre>
	 * @since 2.5
	 */
	boolean inheritLocations() default true;

	/**
	 * Whether or not {@linkplain #initializers context initializers} from test
	 * superclasses should be <em>inherited</em>.
	 *
	 * <p>The default value is {@code true}. This means that an annotated
	 * class will <em>inherit</em> the application context initializers defined
	 * by test superclasses. Specifically, the initializers for a given test
	 * class will be added to the set of initializers defined by test
	 * superclasses. Thus, subclasses have the option of <em>extending</em> the
	 * set of initializers.
	 *
	 * <p>If {@code inheritInitializers} is set to {@code false}, the
	 * initializers for the annotated class will <em>shadow</em> and effectively
	 * replace any initializers defined by superclasses.
	 *
	 * <p>In the following example, the
	 * {@link org.springframework.context.ApplicationContext ApplicationContext}
	 * for {@code ExtendedTest} will be initialized using
	 * {@code BaseInitializer} <strong>and</strong> {@code ExtendedInitializer}.
	 * Note, however, that the order in which the initializers are invoked
	 * depends on whether they implement {@link org.springframework.core.Ordered
	 * Ordered} or are annotated with {@link org.springframework.core.annotation.Order
	 * &#064;Order}.
	 * <pre class="code">
	 * &#064;ContextConfiguration(initializers = BaseInitializer.class)
	 * public class BaseTest {
	 *     // ...
	 * }
	 *
	 * &#064;ContextConfiguration(initializers = ExtendedInitializer.class)
	 * public class ExtendedTest extends BaseTest {
	 *     // ...
	 * }
	 * </pre>
	 * @since 3.2
	 */
	boolean inheritInitializers() default true;

	/**
	 * ApplicationContext}로딩에 사용할 {@link SmartContextLoader} (or {@link ContextLoader}) 타입.
	 *
	 * <p>If not specified, the loader will be inherited from the first superclass
	 * that is annotated with {@code @ContextConfiguration} and specifies an
	 * explicit loader. If no class in the hierarchy specifies an explicit
	 * loader, a default loader will be used instead.
	 *
	 * <p>The default concrete implementation chosen at runtime will be either
	 * {@link org.springframework.test.context.support.DelegatingSmartContextLoader
	 * DelegatingSmartContextLoader} or
	 * {@link org.springframework.test.context.web.WebDelegatingSmartContextLoader
	 * WebDelegatingSmartContextLoader} depending on the absence or presence of
	 * {@link org.springframework.test.context.web.WebAppConfiguration
	 * &#064;WebAppConfiguration}. For further details on the default behavior
	 * of various concrete {@code SmartContextLoaders}, check out the Javadoc for
	 * {@link org.springframework.test.context.support.AbstractContextLoader AbstractContextLoader},
	 * {@link org.springframework.test.context.support.GenericXmlContextLoader GenericXmlContextLoader},
	 * {@link org.springframework.test.context.support.GenericGroovyXmlContextLoader GenericGroovyXmlContextLoader},
	 * {@link org.springframework.test.context.support.AnnotationConfigContextLoader AnnotationConfigContextLoader},
	 * {@link org.springframework.test.context.web.GenericXmlWebContextLoader GenericXmlWebContextLoader},
	 * {@link org.springframework.test.context.web.GenericGroovyXmlWebContextLoader GenericGroovyXmlWebContextLoader}, and
	 * {@link org.springframework.test.context.web.AnnotationConfigWebContextLoader AnnotationConfigWebContextLoader}.
	 *
	 * @since 2.5
	 */
	Class<? extends ContextLoader> loader() default ContextLoader.class;

	/**
	 * The name of the context hierarchy level represented by this configuration.
	 *
	 * <p>If not specified the name will be inferred based on the numerical level
	 * within all declared contexts within the hierarchy.
	 *
	 * <p>This attribute is only applicable when used within a test class hierarchy
	 * that is configured using {@code @ContextHierarchy}, in which case the name
	 * can be used for <em>merging</em> or <em>overriding</em> this configuration
	 * with configuration of the same name in hierarchy levels defined in superclasses.
	 * See the Javadoc for {@link ContextHierarchy @ContextHierarchy} for details.
	 *
	 * @since 3.2.2
	 */
	String name() default "";

}
