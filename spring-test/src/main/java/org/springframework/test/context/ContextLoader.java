/*
 * Copyright 2002-2012 the original author or authors.
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

import org.springframework.context.ApplicationContext;

/**
 * {@link ApplicationContext application context}를 로딩하는 전략 인터페이스,
 * Spring TestContext Framework에 의해 관리되는 통합 테스트 용도.
 *
 * <p><b>노트:</b>스프링 3.1에서, 이 인터페이스 대신에 {@link SmartContextLoader}를 구현하여
 * 어노테이션, 활성화 된 bean definition profiles, 어플리케이션 context initializers를 제공하는 것을 권장함.</p>
 *
 * <p><font color="red">이 인터페이스 대신에 쓰일 {@link SmartContextLoader} 한글화에 집중.</font></p>
 *
 * <p>Clients of a ContextLoader should call
 * {@link #processLocations(Class, String...) processLocations()} prior to
 * calling {@link #loadContext(String...) loadContext()} in case the
 * ContextLoader provides custom support for modifying or generating locations.
 * The results of {@link #processLocations(Class, String...) processLocations()}
 * should then be supplied to {@link #loadContext(String...) loadContext()}.
 *
 * <p>Concrete implementations must provide a {@code public} no-args
 * constructor.
 *
 * <p>Spring provides the following out-of-the-box implementations:
 * <ul>
 * <li>{@link org.springframework.test.context.support.GenericXmlContextLoader GenericXmlContextLoader}</li>
 * <li>{@link org.springframework.test.context.support.GenericPropertiesContextLoader GenericPropertiesContextLoader}</li>
 * </ul>
 *
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 2.5
 * @see SmartContextLoader
 * @see org.springframework.test.context.support.AnnotationConfigContextLoader AnnotationConfigContextLoader
 */
public interface ContextLoader {

	/**
	 * Processes application context resource locations for a specified class.
	 *
	 * <p>Concrete implementations may choose to modify the supplied locations,
	 * generate new locations, or simply return the supplied locations unchanged.
	 *
	 * @param clazz the class with which the locations are associated: used to
	 * determine how to process the supplied locations
	 * @param locations the unmodified locations to use for loading the
	 * application context (can be {@code null} or empty)
	 * @return an array of application context resource locations
	 */
	String[] processLocations(Class<?> clazz, String... locations);

	/**
	 * Loads a new {@link ApplicationContext context} based on the supplied
	 * {@code locations}, configures the context, and finally returns
	 * the context in fully <em>refreshed</em> state.
	 *
	 * <p>Configuration locations are generally considered to be classpath
	 * resources by default.
	 *
	 * <p>Concrete implementations should register annotation configuration
	 * processors with bean factories of {@link ApplicationContext application
	 * contexts} loaded by this ContextLoader. Beans will therefore automatically
	 * be candidates for annotation-based dependency injection using
	 * {@link org.springframework.beans.factory.annotation.Autowired @Autowired},
	 * {@link javax.annotation.Resource @Resource}, and
	 * {@link javax.inject.Inject @Inject}.
	 *
	 * <p>Any ApplicationContext loaded by a ContextLoader <strong>must</strong>
	 * register a JVM shutdown hook for itself. Unless the context gets closed
	 * early, all context instances will be automatically closed on JVM
	 * shutdown. This allows for freeing external resources held by beans within
	 * the context, e.g. temporary files.
	 *
	 * @param locations the resource locations to use to load the application context
	 * @return a new application context
	 * @throws Exception if context loading failed
	 */
	ApplicationContext loadContext(String... locations) throws Exception;

}
