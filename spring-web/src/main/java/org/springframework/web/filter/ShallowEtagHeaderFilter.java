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
	 * Indicates whether the given request and response are eligible for ETag generation.
	 * <p>The default implementation returns {@code true} if all conditions match:
	 * <ul>
	 * <li>response status codes in the {@code 2xx} series</li>
	 * <li>request method is a GET</li>
	 * <li>response Cache-Control header is not set or does not contain a "no-store" directive</li>
	 * </ul>
	 * @param request the HTTP request
	 * @param response the HTTP response
	 * @param responseStatusCode the HTTP response status code
	 * @param responseBody the response body
	 * @return {@code true} if eligible for ETag generation; {@code false} otherwise
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
	 * Generate the ETag header value from the given response body byte array.
	 * <p>The default implementation generates an MD5 hash.
	 * @param bytes the response body as byte array
	 * @return the ETag header value
	 * @see org.springframework.util.DigestUtils
	 */
	protected String generateETagHeaderValue(byte[] bytes) {
		StringBuilder builder = new StringBuilder("\"0");
		DigestUtils.appendMd5DigestAsHex(bytes, builder);
		builder.append('"');
		return builder.toString();
	}

}
