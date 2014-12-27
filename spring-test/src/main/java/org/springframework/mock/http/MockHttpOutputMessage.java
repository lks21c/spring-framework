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

package org.springframework.mock.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;

/**
 * {@link HttpOutputMessage} 인터페이스의 Mock 구현체.
 *
 * @author Rossen Stoyanchev
 * @since 3.2
 */
public class MockHttpOutputMessage implements HttpOutputMessage {

	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private final HttpHeaders headers = new HttpHeaders();

	private final ByteArrayOutputStream body = new ByteArrayOutputStream(1024);


	/**
	 * Return 헤더를 리턴
	 */
	@Override
	public HttpHeaders getHeaders() {
		return this.headers;
	}

	/**
	 * Return Body 컨텐츠를 리턴.
	 */
	@Override
	public OutputStream getBody() throws IOException {
		return this.body;
	}

	/**
	 * Return Body 컨텐츠를 byte array로 리턴.
	 */
	public byte[] getBodyAsBytes() {
		return this.body.toByteArray();
	}

	/**
	 * Return Body 컨텐츠를 UTF-8 String으로 리턴.
	 */
	public String getBodyAsString() {
		return getBodyAsString(DEFAULT_CHARSET);
	}

	/**
	 * Return Body 컨텐츠를 String으로 리턴.
	 * @param charset 리턴할 String의 {@link java.nio.charset.Charset}을 지정
	 */
	public String getBodyAsString(Charset charset) {
		byte[] bytes = getBodyAsBytes();
		try {
			return new String(bytes, charset.name());
		}
		catch (UnsupportedEncodingException ex) {
			// 발생해서는 안됨
			throw new IllegalStateException(ex);
		}
	}

}
