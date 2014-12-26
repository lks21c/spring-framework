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
import org.springframework.util.ObjectUtils;

import javax.servlet.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <p>{@link javax.servlet.FilterChain} 인터페이스의 Mock 구현. Used
 * web framework테스트에 사용함; 또한 커스텀 {@link javax.servlet.Filter} 구현 테스트에 유용함.
 * </p>
 *
 * <p>
 * {@link MockFilterChain}는 하나 이상의 필터를 설정할수 있고 하나의 서블릿을 호출할 수 있음.
 * 처음에 체인이 호출될때, 모든 필터와 서블릿을 호출하고 request와 response를 저장한다.
 * {@link #reset()}이 호출되는게 아니라면 중복 호출은 {@link IllegalStateException}를 호출한다.
 * </p>
 *
 * @author Juergen Hoeller
 * @author Rob Winch
 * @author Rossen Stoyanchev
 * @see MockFilterConfig
 * @see PassThroughFilterChain
 * @since 2.0.3
 */
public class MockFilterChain implements FilterChain {

    private ServletRequest request;

    private ServletResponse response;

    private final List<Filter> filters;

    private Iterator<Filter> iterator;


    /**
     * 아무것도 하지않는 단일 {@link Filter} 등록.
     * 첫 호출이 reuqest와 response를 저장함.
     * {@link #reset()}이 호출되는게 아니라면 중복 호출은 {@link IllegalStateException}를 호출한다.
     *
     */
    public MockFilterChain() {
        this.filters = Collections.emptyList();
    }

    /**
     * 주어진 Servlet을 가지고 {@code FilterChain}를 생성함.
     *
     * @param servlet 이 {@link FilterChain}에서 호출할 {@link Servlet}
     * @since 3.2
     */
    public MockFilterChain(Servlet servlet) {
        this.filters = initFilterList(servlet);
    }

    /**
     * 주어진 Servlet과 Filter를 가지고 {@code FilterChain}를 생성함.
     *
     * @param servlet 이 {@link FilterChain}에서 호출할 {@link Servlet}
     * @param filters 이 {@link FilterChain}에서 호출할 {@link Filter}들
     * @since 3.2
     */
    public MockFilterChain(Servlet servlet, Filter... filters) {
        Assert.notNull(filters, "filters cannot be null");
        Assert.noNullElements(filters, "filters cannot contain null values");
        this.filters = initFilterList(servlet, filters);
    }

    private static List<Filter> initFilterList(Servlet servlet, Filter... filters) {
        Filter[] allFilters = ObjectUtils.addObjectToArray(filters, new ServletFilterProxy(servlet));
        return Arrays.asList(allFilters);
    }

    /**
     * {@link #doFilter}에서 쓰여진 request 리턴
     */
    public ServletRequest getRequest() {
        return this.request;
    }

    /**
     * {@link #doFilter}에서 쓰여진 response 리턴
     */
    public ServletResponse getResponse() {
        return this.response;
    }

    /**
     * 등록된 {@link Filter}들과(또는) {@link Servlet}을 호출함.
     * 또한 request 와 response를 저장함.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        Assert.notNull(request, "Request must not be null");
        Assert.notNull(response, "Response must not be null");

        if (this.request != null) {
            throw new IllegalStateException("This FilterChain has already been called!");
        }

        if (this.iterator == null) {
            this.iterator = this.filters.iterator();
        }

        if (this.iterator.hasNext()) {
            Filter nextFilter = this.iterator.next();
            nextFilter.doFilter(request, response, this);
        }

        this.request = request;
        this.response = response;
    }

    /**
     * {@link MockFilterChain}을 Reset함.(나중에 다시 호출하기 위해)
     */
    public void reset() {
        this.request = null;
        this.response = null;
        this.iterator = null;
    }


    /**
     * Servlet으로 단순히 전달하는 Filter.
     */
    private static class ServletFilterProxy implements Filter {

        private final Servlet delegateServlet;

        private ServletFilterProxy(Servlet servlet) {
            Assert.notNull(servlet, "servlet cannot be null");
            this.delegateServlet = servlet;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            this.delegateServlet.service(request, response);
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void destroy() {
        }

        @Override
        public String toString() {
            return this.delegateServlet.toString();
        }
    }

}
