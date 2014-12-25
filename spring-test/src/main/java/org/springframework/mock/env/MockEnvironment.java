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

package org.springframework.mock.env;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 간단한 {@link ConfigurableEnvironment}의 구현으로 {@link #setProperty(String, String)} 와
 * {@link #withProperty(String, String)} 메서드를 테스트 목적으로 제공한다.
 *
 * @author Chris Beams
 * @author Sam Brannen
 * @since 3.2
 * @see org.springframework.mock.env.MockPropertySource
 */
public class MockEnvironment extends AbstractEnvironment {

	private MockPropertySource propertySource = new MockPropertySource();

	/**
	 * 하나의 {@link MockPropertySource}를 가진 {@code MockEnvironment}를 생성함.
	 */
	public MockEnvironment() {
		getPropertySources().addLast(propertySource);
	}

	/**
	 * MockEnvironment의 {@link MockPropertySource}에 property를 set함.
	 */
	public void setProperty(String key, String value) {
		propertySource.setProperty(key, value);
	}

	/**
	 * 편리하게 {@link #setProperty} 대신 쓸수 있는 메서드. fluent-style(역자주 : Build Up 패턴)으로 프로퍼티를
	 * set할때 사용하기 편리함.
	 * 
	 * @return 현재 {@link MockEnvironment} 인스턴스를 리턴함
	 * @see MockPropertySource#withProperty
	 */
	public MockEnvironment withProperty(String key, String value) {
		this.setProperty(key, value);
		return this;
	}

}
