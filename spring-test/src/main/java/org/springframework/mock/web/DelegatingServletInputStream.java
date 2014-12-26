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

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link javax.servlet.ServletInputStream}를 전달하는 구현.
 *
 * <p>
 * {@link MockHttpServletRequest}에서 사용함; 일반적으로 testing application controller에서 직접적으로 쓰지 않음.
 * </p>
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see MockHttpServletRequest
 */
public class DelegatingServletInputStream extends ServletInputStream {

	private final InputStream sourceStream;


	/**
	 *
	 * 주어진 InputStream으로 DelegatingServletInputStream을 생성함.
	 * @param sourceStream source stream(절대 {@code null}이면 안됨)
	 */
	public DelegatingServletInputStream(InputStream sourceStream) {
		Assert.notNull(sourceStream, "Source InputStream must not be null");
		this.sourceStream = sourceStream;
	}

	/**
	 * source stream을 리턴함(절대 {@code null}이면 안됨).
	 */
	public final InputStream getSourceStream() {
		return this.sourceStream;
	}


	@Override
	public int read() throws IOException {
		return this.sourceStream.read();
	}

	@Override
	public void close() throws IOException {
		super.close();
		this.sourceStream.close();
	}

}
