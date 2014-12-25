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

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Properties;

/**
 * 유닛 테스트를 위한 간단한 {@link PropertySource} 구현. <br />
 * 사용자가 지정한 프로퍼티 이름과 프로퍼티를 사용하거나, <br />
 * {@code MOCK_PROPERTIES_PROPERTY_SOURCE_NAME} 이름으로 프로퍼티를 생성 할 수 있음. <br />
 * 
 *
 * @author Chris Beams
 * @since 3.1
 * @see org.springframework.mock.env.MockEnvironment
 */
public class MockPropertySource extends PropertiesPropertySource {

	/**
	 * 이 값{@value} 은 생성자에서 이름을 정해주지 않을때 쓰는 {@link MockPropertySource}의 기본 이름
	 *
	 * @see #MockPropertySource()
	 * @see #MockPropertySource(String)
	 */
	public static final String MOCK_PROPERTIES_PROPERTY_SOURCE_NAME = "mockProperties";

	/**
	 * {@code MockPropertySource} 생성자 <br />
	 * 프로퍼티 이름 : {@value #MOCK_PROPERTIES_PROPERTY_SOURCE_NAME}로 지정 <br />
	 * 프로퍼티 : 프로퍼티는 내부에서 인스턴스 생성.
	 */
	public MockPropertySource() {
		this(new Properties());
	}

	/**
	 * {@code MockPropertySource} 생성자 <br />
	 * 프로퍼티 이름 : 사용할 프로퍼티 이름을 파라미터로 지정 <br />
	 * 프로퍼티 : 프로퍼티는 내부에서 인스턴스 생성.
	 * 
	 * @param name Properties 소스의 이름을 지정
	 */
	public MockPropertySource(String name) {
		this(name, new Properties());
	}

	/**
	 * {@code MockPropertySource} 생성자 <br />
	 * 프로퍼티 이름 : {@value #MOCK_PROPERTIES_PROPERTY_SOURCE_NAME}로 지정 <br />
	 * 프로퍼티 : 사용할 프로퍼티를 파라미터로 지정
	 * 
	 * @param {@link MockPropertySource}에서 사용한 Properties를 지정
	 */
	public MockPropertySource(Properties properties) {
		this(MOCK_PROPERTIES_PROPERTY_SOURCE_NAME, properties);
	}

	/**
	 * {@code MockPropertySource} 생성자 <br />
	 * 프로퍼티 이름 : 사용할 프로퍼티 이름을 파라미터로 지정 <br />
	 * 프로퍼티 : 사용할 프로퍼티를 파라미터로 지정
	 * 
	 * @param name 사용할 프로퍼티 이름
	 * @param properties 사용할 프로퍼티
	 */
	public MockPropertySource(String name, Properties properties) {
		super(name, properties);
	}

	/**
	 * {@link Properties}에 프로퍼티를 set함.
	 */
	public void setProperty(String name, Object value) {
		this.source.put(name, value);
	}

	/**
	 * setProperty를 fluent-style로 사용할 때 활용.
	 * 
	 * @return {@link MockPropertySource} 인스턴스
	 */
	public MockPropertySource withProperty(String name, Object value) {
		this.setProperty(name, value);
		return this;
	}

}
