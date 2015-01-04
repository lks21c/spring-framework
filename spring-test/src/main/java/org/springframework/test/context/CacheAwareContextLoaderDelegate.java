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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;

/**
 * {@code CacheAwareContextLoaderDelegate} 역할은 어플리케이션 context의 {@linkplain
 * #loadContext loading}과 {@linkplain #closeContext closing}를 수행할 시 뒷단에서
 * 투명하게 <em>context cache</em>와 정보를 교환하는 역할을 함.
 *
 * <p>노트: {@code CacheAwareContextLoaderDelegate}는 {@link ContextLoader}또는 {@link SmartContextLoader} 인터페이스를
 * 확장하지 않음.</p>
 *
 *
 * @author Sam Brannen
 * @since 3.2.2
 */
public interface CacheAwareContextLoaderDelegate {

	/**
	 * 제공된 {@link MergedContextConfiguration}를 위해 {@linkplain ApplicationContext application context}을
	 * 읽어 주어진 {@code MergedContextConfiguration}에 설정된 {@link ContextLoader}로 전달함.
     *
     * <p>context가 <em>context 캐시</em>에 존재하면 바로 리턴함;그렇지 않을경우, 로드되어, 캐시에 저장되고 리턴함.</p>
	 * @param mergedContextConfiguration 어플리케이션 context 로드에 쓰일 merged context configuration;절대 {@code null}이면 안됨
	 * @return application context
	 * @throws IllegalStateException 어플리케이션 conteXT 로딩하는데 에러 발생 시
	 */
	ApplicationContext loadContext(MergedContextConfiguration mergedContextConfiguration);

	/**
     * {@link ConfigurableApplicationContext}의 인스턴스라면,
     * 주어진 {@link MergedContextConfiguration}의 {@linkplain ApplicationContext application context}를
     * <em>context 캐시</em>에서 제거하고 {@linkplain ConfigurableApplicationContext#close() 종료함}.
	 * Remove the {@linkplain ApplicationContext application context} for the
	 * supplied {@link MergedContextConfiguration} from the <em>context cache</em>
	 * and {@linkplain ConfigurableApplicationContext#close() close} it if it is
	 * an instance of {@link ConfigurableApplicationContext}.
     *
     * <p>추가로, {@code HierarchyMode}를 준수함. Javadoc으로 {@link HierarchyMode}의 상세사항을 참고할것. </p>

     * <p>일반적으로 싱글턴 빈의 상태가 바뀌었을때,이 메서드만 호출하면 됨.(잠재적으로 추후 context와의 정보교환에 영향을 끼치는) </p>
     *
	 * @param mergedContextConfiguration 종료할 어플리케이션 context의 merged context configuration;절대 {@code null}이면 안됨
     * @param hierarchyMode hierarchy mode; context가 구조를 이루지 않는다면 {@code null} 가능
	 * @since 4.1
	 */
	void closeContext(MergedContextConfiguration mergedContextConfiguration, HierarchyMode hierarchyMode);

}
