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

package org.springframework.web.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

/**
 * {@link javax.servlet.Filter}로써 response의 content에 기반해 {@code ETag} 값을 생성함. <br />
 * 이 ETAG 값은 request 헤더의 {@code If-None-Match} 값과 비교됨. <br />
 * 만약 이 헤더값들이 같으면, response content는 전송되지 않고 {@code 304 "Not Modified"} status를 대신 리턴함. <br />
 * 
 * <p> ETag는 response content에 기반하기 때문에, {@link org.springframework.web.servlet.View}는 여전히 렌더링됨.
 * 이 필터는 서버 퍼포먼스가 아닌 bandwidth만 절약 됨.
 * 
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.0
 */
public class ShallowEtagHeaderFilter extends OncePerRequestFilter {

	private static final String HEADER_ETAG = "ETag";

	private static final String HEADER_IF_NONE_MATCH = "If-None-Match";

	private static final String HEADER_CACHE_CONTROL = "Cache-Control";

	private static final String DIRECTIVE_NO_STORE = "no-store";


	/** Checking for Servlet 3.0+ HttpServletResponse.getHeader(String) */
	private static final boolean responseGetHeaderAvailable =
			ClassUtils.hasMethod(HttpServletResponse.class, "getHeader", String.class);


	/**
	 * The default value is "false" so that the filter may delay the generation of
	 * an ETag until the last asynchronously dispatched thread.
	 */
	@Override
	protected boolean shouldNotFilterAsyncDispatch() {
		return false;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		HttpServletResponse responseToUse = response;
		if (!isAsyncDispatch(request) && !(response instanceof ContentCachingResponseWrapper)) {
			responseToUse = new ContentCachingResponseWrapper(response);
		}

		filterChain.doFilter(request, responseToUse);

		if (!isAsyncStarted(request)) {
			updateResponse(request, responseToUse);
		}
	}

	private void updateResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ContentCachingResponseWrapper responseWrapper =
				WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
		Assert.notNull(responseWrapper, "ShallowEtagResponseWrapper not found");

		HttpServletResponse rawResponse = (HttpServletResponse) responseWrapper.getResponse();
		int statusCode = responseWrapper.getStatusCode();
		byte[] body = responseWrapper.getContentAsByteArray();

		if (rawResponse.isCommitted()) {
			if (body.length > 0) {
				StreamUtils.copy(body, rawResponse.getOutputStream());
			}
		}
		else if (isEligibleForEtag(request, responseWrapper, statusCode, body)) {
			String responseETag = generateETagHeaderValue(body);
			rawResponse.setHeader(HEADER_ETAG, responseETag);
			String requestETag = request.getHeader(HEADER_IF_NONE_MATCH);
			if (responseETag.equals(requestETag)) {
				if (logger.isTraceEnabled()) {
					logger.trace("ETag [" + responseETag + "] equal to If-None-Match, sending 304");
				}
				rawResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("ETag [" + responseETag + "] not equal to If-None-Match [" + requestETag +
							"], sending normal response");
				}
				if (body.length > 0) {
					rawResponse.setContentLength(body.length);
					StreamUtils.copy(body, rawResponse.getOutputStream());
				}
			}
		}
		else {
			if (logger.isTraceEnabled()) {
				logger.trace("Response with status code [" + statusCode + "] not eligible for ETag");
			}
			if (body.length > 0) {
				rawResponse.setContentLength(body.length);
				StreamUtils.copy(body, rawResponse.getOutputStream());
			}
		}
	}

	/**
	 * 주어진 request와 response가 ETag 생성에 적합한지 나타냄.
	 * <p> 기본 구현은 아래의 조건이 모두 만족할때 {@code true}를 리턴함.
	 * <ul>
	 * <li>response status 코드가 {@code 2xx}일때</li>
	 * <li>request method가 GET일때</li>
	 * <li>response Cache-Control 헤더가 설정되어 있지 않거나 "no-store" 디렉티브를 포함하지 않을 때</li>
	 * </ul>
	 * @param request HTTP request
	 * @param response HTTP response
	 * @param responseStatusCode HTTP response status code
	 * @param responseBody response body
	 * @return {@code true} 만약 ETag 생성이 적합한 경우;나머지 경우는 {@code false}
	 */
	protected boolean isEligibleForEtag(HttpServletRequest request, HttpServletResponse response,
			int responseStatusCode, byte[] responseBody) {

		if (responseStatusCode >= 200 && responseStatusCode < 300 &&
				HttpMethod.GET.name().equals(request.getMethod())) {
			String cacheControl = (responseGetHeaderAvailable ? response.getHeader(HEADER_CACHE_CONTROL) : null);
			if (cacheControl == null || !cacheControl.contains(DIRECTIVE_NO_STORE)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * response body byte array로부터 ETag 헤더값을 생성함.
	 * 
	 * <p> 기본 구현은 MD5 해시를 생성함.
	 * @param bytes response body의 byte array
	 * @return ETag 헤더 값
	 * @see org.springframework.util.DigestUtils
	 */
	protected String generateETagHeaderValue(byte[] bytes) {
		StringBuilder builder = new StringBuilder("\"0");
		DigestUtils.appendMd5DigestAsHex(bytes, builder);
		builder.append('"');
		return builder.toString();
	}

}
