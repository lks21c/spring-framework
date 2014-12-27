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
package org.springframework.mock.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.util.Assert;

/**
 * {@link HttpInputMessage} 인터페이스의 Mock 구현체.
 *
 * @author Rossen Stoyanchev
 * @since 3.2
 */
public class MockHttpInputMessage implements HttpInputMessage {

	/**
	 * {@link HttpHeaders} 참고
	 */
	private final HttpHeaders headers = new HttpHeaders();

	/**
	 * Http Body
	 */
	private final InputStream body;

	/**
	 * MockHttpInputMessage 생성자
	 * @param contents body값으로 쓸 contents 지정
	 */
	public MockHttpInputMessage(byte[] contents) {
		this.body = (contents != null) ? new ByteArrayInputStream(contents) : null;
	}

	/**
	 * MockHttpInputMessage 생성자
	 * @param body body값으로 쓸 contents 지정
	 */
	public MockHttpInputMessage(InputStream body) {
		Assert.notNull(body, "'body' must not be null");
		this.body = body;
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.headers;
	}

	@Override
	public InputStream getBody() throws IOException {
		return this.body;
	}
}
