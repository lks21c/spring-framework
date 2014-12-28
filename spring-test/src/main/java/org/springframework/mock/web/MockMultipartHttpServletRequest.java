/*
 * Copyright 2002-2013 the original author or authors.
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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletContext;
import java.util.*;

/**
 * {@link org.springframework.web.multipart.MultipartHttpServletRequest} 인터페이스의 mock 구현.
 *
 * <p>Spring Framework 4.0에서, 이 클래스는 Servlet 3.0으로 디자인됨.
 *
 * <p>멀티파트 업로드에 접근하는 어플리케이션 컨트롤러 테스트에 유용함.
 * The {@link MockMultipartFile}이 이 mock request로 인해 쓰여질 수 있음.
 *
 * @author Juergen Hoeller
 * @author Eric Crampton
 * @author Arjen Poutsma
 * @since 2.0
 * @see MockMultipartFile
 */
public class MockMultipartHttpServletRequest extends MockHttpServletRequest implements MultipartHttpServletRequest {

	private final MultiValueMap<String, MultipartFile> multipartFiles =
			new LinkedMultiValueMap<String, MultipartFile>();


	/**
	 * {@link MockServletContext}로 {@code MockMultipartHttpServletRequest}을 생성함.
	 * @see #MockMultipartHttpServletRequest(ServletContext)
	 */
	public MockMultipartHttpServletRequest() {
		this(null);
	}

	/**
	 * 주어진 {@link ServletContext}로 {@code MockMultipartHttpServletRequest}을 생성함.
	 * @param servletContext request가 실행 될 ServletContext
	 * ({@code null}일때 기본 {@link MockServletContext}을 사용함).
	 */
	public MockMultipartHttpServletRequest(ServletContext servletContext) {
		super(servletContext);
		setMethod("POST");
		setContentType("multipart/form-data");
	}


	/**
	 * 이 request에 파일을 추가함.
	 * 멀티파트 form의 파라미터 이름은 {@link MultipartFile#getName()}로 부터 획득됨.
	 * @param file 추가할 멀티파트 파일
	 */
	public void addFile(MultipartFile file) {
		Assert.notNull(file, "MultipartFile must not be null");
		this.multipartFiles.add(file.getName(), file);
	}

	@Override
	public Iterator<String> getFileNames() {
		return this.multipartFiles.keySet().iterator();
	}

	@Override
	public MultipartFile getFile(String name) {
		return this.multipartFiles.getFirst(name);
	}

	@Override
	public List<MultipartFile> getFiles(String name) {
		List<MultipartFile> multipartFiles = this.multipartFiles.get(name);
		if (multipartFiles != null) {
			return multipartFiles;
		}
		else {
			return Collections.emptyList();
		}
	}

	@Override
	public Map<String, MultipartFile> getFileMap() {
		return this.multipartFiles.toSingleValueMap();
	}

	@Override
	public MultiValueMap<String, MultipartFile> getMultiFileMap() {
		return new LinkedMultiValueMap<String, MultipartFile>(this.multipartFiles);
	}

	@Override
	public String getMultipartContentType(String paramOrFileName) {
		MultipartFile file = getFile(paramOrFileName);
		if (file != null) {
			return file.getContentType();
		}
		else {
			return null;
		}
	}

	@Override
	public HttpMethod getRequestMethod() {
		return HttpMethod.valueOf(getMethod());
	}

	@Override
	public HttpHeaders getRequestHeaders() {
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.put(headerName, Collections.list(getHeaders(headerName)));
		}
		return headers;
	}

	@Override
	public HttpHeaders getMultipartHeaders(String paramOrFileName) {
		String contentType = getMultipartContentType(paramOrFileName);
		if (contentType != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", contentType);
			return headers;
		}
		else {
			return null;
		}
	}

}
