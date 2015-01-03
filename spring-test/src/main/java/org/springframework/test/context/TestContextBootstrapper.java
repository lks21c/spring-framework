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

import java.util.List;

/**
 * {@code TestContextBootstrapper}는 <em>Spring TestContext Framework</em> 로딩을 위한 전략 SPI를 정의함.
 *
 * <p> 커스텀 로딩 전략이 테스트 클래스를 위해 설정될수 있음;직접적으로 {@link BootstrapWith @BootstrapWith}를 쓰거나
 * 메타 어노테이션을 통해서.
 * </p>
 * See {@link org.springframework.test.context.web.WebAppConfiguration @WebAppConfiguration}
 * for an example.
 *
 * <p>{@link TestContextManager}는 {@code TestContextBootstrapper}를 사용하여 현재 테스트를 위해
 * {@linkplain #getTestExecutionListeners TestExecutionListeners}를 얻고 그것이 관리하는 {@link TestContext} 생성에 필요한
 * {@linkplain #buildMergedContextConfiguration 통합된 context configuration을 생성함}.
 * </p>
 *
 * <p>구체적인 구현체는 반드시 인자가 없는 {@code public} 생성자를 제공해야함.</p>
 *
 * <p>
 * <strong>노트</strong>: 이 SPI는 새 요구사항을 만족시키기 위해 잠재적으로 미래에 변경될지 모름.
 * 구현자들은 그러므로 이 인터페이스를 직접적으로 구현하지 <em>않기를</em> 권장함, {@link org.springframework.test.context.support.AbstractTestContextBootstrapper
 * AbstractTestContextBootstrapper}(또는 자식클래스)를 상속받는것을 권장함.
 * </p>
 *
 * @author Sam Brannen
 * @since 4.1
 * @see BootstrapWith
 * @see BootstrapContext
 */
public interface TestContextBootstrapper {

	/**
	 * {@link BootstrapContext} 설정.
	 */
	void setBootstrapContext(BootstrapContext bootstrapContext);

	/**
	 * {@link BootstrapContext} 리턴.
	 */
	BootstrapContext getBootstrapContext();

	/**
	 * 새롭게 인스턴스로 만든 {@link TestExecutionListener TestExecutionListeners}리스트를 얻음,
	 * 이 부트스트래퍼와 {@link BootstrapContext}의 테스트 클래스.
	 * <em>존재하지 않으면</em>, <em>기본</em> 리스너가 반드시 리턴됨.
	 * 게다가, 기본 리스터들은 {@link org.springframework.core.annotation.AnnotationAwareOrderComparator
	 * AnnotationAwareOrderComparator}로 반드시 정렬되어야 함.
	 *
	 * <p>
	 * 구현체들은 어떤 기본 리스너들의 집합으로 구성할지 자유롭게 구현할수 있음.
	 * 그러나, 기본적으로, Spring TestContext Framework는 {@link org.springframework.core.io.support.SpringFactoriesLoader SpringFactoriesLoader}를
	 * 사용할 것임;클래스 패스의 모든 {@code META-INF/spring.factories} 파일들에 설정된 모든 {@code TestExecutionListener} 클래스 이름을 lookup하기 위해.
	 * </p>
	 * <p>{@link TestExecutionListeners @TestExecutionListeners}의 {@link TestExecutionListeners#inheritListeners() inheritListeners} 플래그는
	 * 반드시 고려되어야 함. 구체적으로, 만약 {@code inheritListeners} 플래그가 {@code true}이면, 주어진 테스트를
	 * 위해 정의된 리스너들은 반드시 부모 클래스에 정의된 리스너들 리스트 뒤에 덧붙여짐.
	 * @return {@code TestExecutionListener} 인스턴스 리스트
	 */
	List<TestExecutionListener> getTestExecutionListeners();

	/**
	 * 이 부트스트래퍼와 연관된 {@link BootstrapContext} 안에 있는
	 * 테스트 클래스를 위해 {@linkplain MergedContextConfiguration merged context configuration}을 생성함.
	 *
	 * <p>구현체들은 합쳐진 설정을 생성 시 반드시 아래의 사항을 고려해야함:</p>
	 * <ul>
	 *
	 * </ul>
	 * <ul>
	 * <li>Context hierarchies declared via {@link ContextHierarchy @ContextHierarchy}
	 * and {@link ContextConfiguration @ContextConfiguration}</li>
	 * <li>Active bean definition profiles declared via {@link ActiveProfiles @ActiveProfiles}</li>
	 * <li>{@linkplain org.springframework.context.ApplicationContextInitializer
	 * Context initializers} declared via {@link ContextConfiguration#initializers}</li>
	 * </ul>
	 * <p>Consult the Javadoc for the aforementioned annotations for details on
	 * the required semantics.
	 * <p>When determining which {@link ContextLoader} to use for a given test
	 * class, the following algorithm should be used:
	 * <ol>
	 * <li>If a {@code ContextLoader} class has been explicitly declared via
	 * {@link ContextConfiguration#loader}, use it.</li>
	 * <li>Otherwise, concrete implementations are free to determine which
	 * {@code ContextLoader} class to use as as default.</li>
	 * </ol>
	 * @return the merged context configuration, never {@code null}
	 */
	MergedContextConfiguration buildMergedContextConfiguration();

}
