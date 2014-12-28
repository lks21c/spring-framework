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

import javax.servlet.*;
import java.io.IOException;

/**
 * {@link javax.servlet.FilterConfig}의 구현.
 * 단순히 Filter/FilterChain 또는 Servlet으로 전달.
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see javax.servlet.Filter
 * @see javax.servlet.Servlet
 * @see MockFilterChain
 */
public class PassThroughFilterChain implements FilterChain {

	private Filter filter;

	private FilterChain nextFilterChain;

	private Servlet servlet;


	/**
	 * PassThroughFilterChain를 생성.
	 * 주어진 필터와 다음 필터 체인을 지정.
	 *
	 * @param filter 전달할 필터
	 * @param nextFilterChain 다음 필터를 위한 필터체인
	 */
	public PassThroughFilterChain(Filter filter, FilterChain nextFilterChain) {
		Assert.notNull(filter, "Filter must not be null");
		Assert.notNull(nextFilterChain, "'FilterChain must not be null");
		this.filter = filter;
		this.nextFilterChain = nextFilterChain;
	}

	/**
	 * PassThroughFilterChain를 생성.
	 * 주어진 Servlet으로 전달.
	 * @param servlet 전달할 Servlet
	 */
	public PassThroughFilterChain(Servlet servlet) {
		Assert.notNull(servlet, "Servlet must not be null");
		this.servlet = servlet;
	}


	/**
	 * 호출을 필터/Servlet으로 전달
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		if (this.filter != null) {
			this.filter.doFilter(request, response, this.nextFilterChain);
		}
		else {
			this.servlet.service(request, response);
		}
	}

}
