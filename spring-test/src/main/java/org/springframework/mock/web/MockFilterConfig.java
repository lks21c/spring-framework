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

package org.springframework.mock.web;

import org.springframework.util.Assert;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link javax.servlet.FilterConfig} 인터페이스의 mock 구현.
 *
 * <p>web framework테스트에 사용함; 또한 커스텀 {@link javax.servlet.Filter} 구현 테스트에 유용함.
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see MockFilterChain
 * @see PassThroughFilterChain
 */
public class MockFilterConfig implements FilterConfig {

	private final ServletContext servletContext;

	private final String filterName;

	private final Map<String, String> initParameters = new LinkedHashMap<String, String>();


	/**
	 * 기본 {@link MockServletContext}와 함께 MockFilterConfig를 생성함.
	 */
	public MockFilterConfig() {
		this(null, "");
	}

	/**
	 * 기본 {@link MockServletContext}와 함께 MockFilterConfig를 생성함.
	 * @param filterName 필터이름
	 */
	public MockFilterConfig(String filterName) {
		this(null, filterName);
	}

	/**
	 * CMockFilterConfig를 생성함.
	 * @param servletContext the ServletContext that the servlet runs in
	 */
	public MockFilterConfig(ServletContext servletContext) {
		this(servletContext, "");
	}

	/**
	 * Create a new MockFilterConfig.
	 * @param servletContext servlet이 실행될 ServletContext
	 * @param filterName 필터이름
	 */
	public MockFilterConfig(ServletContext servletContext, String filterName) {
		this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
		this.filterName = filterName;
	}


	@Override
	public String getFilterName() {
		return filterName;
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	public void addInitParameter(String name, String value) {
		Assert.notNull(name, "Parameter name must not be null");
		this.initParameters.put(name, value);
	}

	@Override
	public String getInitParameter(String name) {
		Assert.notNull(name, "Parameter name must not be null");
		return this.initParameters.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return Collections.enumeration(this.initParameters.keySet());
	}

}
