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
     * {@link org.springframework.context.ApplicationContext ApplicationContext}를 로딩하는데 쓰는
     * 리소스 위치들.
     *
     * <p>런타임에 location이 어떻게 인터프리트 되는지(특히 상대 경로의 경우) 자세한 정보는 Javadoc {@link org.springframework.test.context.support.AbstractContextLoader#modifyLocations
     * AbstractContextLoader.modifyLocations()}를 확일 할 것.
     * 또한, 아무것도 명시되지 않았을대 쓰일 기본 위치에 대한 상세 정보는 {@link org.springframework.test.context.support.AbstractContextLoader#generateDefaultLocations
     * AbstractContextLoader.generateDefaultLocations()}를 참고 할 것.
     * </p>
	 *
     * <p>노트: 위의 기본 룰은 {@link org.springframework.test.context.support.AbstractContextLoader
     * AbstractContextLoader}의 자식 클래스 들에 적용됨.
     * 예: {@link org.springframework.test.context.support.GenericXmlContextLoader GenericXmlContextLoader} 또는
     * {@link org.springframework.test.context.support.GenericGroovyXmlContextLoader GenericGroovyXmlContextLoader}
     * (만약 {@code locations}이 설정되었을 때 쓰여질 효과전 기본 구현체)
     * 기본 로더에 관한 자세한 정보는 {@link #loader}를 참고할 것.
     * </p>
     *
     * <p>{@link #value} 대신에 쓰는 것은 괜찮지만 동시에 같이 쓰지 말 것.</p>
     *
	 * @since 2.5
	 * @see #inheritLocations
	 */
	String[] locations() default {};

	/**
     * {@link org.springframework.context.ApplicationContext ApplicationContext} 로딩에 쓰일
     * <em>어노테이션 클래스</em>.
	 *
     * <p><em>어노테이션 클래스</em>가 명시되지 않았을 때 기본 설정 클래스가 어떻게 감지되는지 자세한 정보는
     * {@link org.springframework.test.context.support.AnnotationConfigContextLoader#detectDefaultConfigurationClasses
     * AnnotationConfigContextLoader.detectDefaultConfigurationClasses()}를 참고할 것.
     *  기본 로더에 관한 자세한 정보는 {@link #loader}를 참고할 것.
     *  </p>
	 *
	 * @since 3.1
	 * @see org.springframework.context.annotation.Configuration
	 * @see org.springframework.test.context.support.AnnotationConfigContextLoader
	 * @see #inheritLocations
	 */
	Class<?>[] classes() default {};

	/**
     * {@link ConfigurableApplicationContext}를 initializing 하는데 쓰일 어플리케이션 context
     * <em>initializer 클래스</em>.
	 *
     * <p></p>
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
     * <p>명시하지 않을 시, {@code @ContextConfiguration}과 로더를 명시한 첫 부모클래스로부터
     * 로더를 상솓 받음. 만약 어떤 상위 구조도 로더를 명시하지 않으면, 기본 로더가 사용됨.
     *
     * 런타임에 선택될 기본 구현체는 {@link org.springframework.test.context.support.DelegatingSmartContextLoader
     * DelegatingSmartContextLoader} 또는 {@link org.springframework.test.context.web.WebDelegatingSmartContextLoader
     * WebDelegatingSmartContextLoader}가 {@link org.springframework.test.context.web.WebAppConfiguration
     * &#064;WebAppConfiguration} 존재여부에 따라 결정 됨.
     *
     * 다양한 {@code SmartContextLoaders} 구현체의 자세한 정보는 아래를 참고할 것.
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
     *
     * 이 설정에서 표현된 context 구조 level의 이름.
     *
     * <p>만약 명시되지 않으면 이 구조안에서 정의된 모든 컨텍스트의 숫자 레벨.</p>
	 *
     * <p>이 어트리뷰트는 {@code @ContextHierarchy}를 사용한 test 클래스 구조에서만 사용가능함.
     * 이름은 <em>merging</em> 또는 <em>overriding</em> 용도로 사용가능하고 부모 클래스에서 정의된
     * 동일한 hierachy level 이름으로 사용 가능.
     * 상세정보는 {@link ContextHierarchy @ContextHierarchy}를 볼 것.
     * </p>
	 *
	 * @since 3.2.2
	 */
	String name() default "";

}
