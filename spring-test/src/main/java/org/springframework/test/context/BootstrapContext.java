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

/**
 * {@code BootstrapContext}는 <em>Spring TestContext Framework</em>이  로딩될 context를 encapsulation함.
 *
 * @author Sam Brannen
 * @since 4.1
 * @see BootstrapWith
 * @see TestContextBootstrapper
 */
public interface BootstrapContext {

	/**
	 * 이 부트 스트랩 컨텍스트를 위한 {@link Class test class}를 얻음.
	 * @return 테스트 클래스(절대 {@code null}이면 안됨)
	 */
	Class<?> getTestClass();

	/**
	 * <em>context cache</em>와 투명한 상호교환에 쓰일 {@link CacheAwareContextLoaderDelegate}를 얻음.
	 */
	CacheAwareContextLoaderDelegate getCacheAwareContextLoaderDelegate();

}
