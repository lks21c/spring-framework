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

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link javax.servlet.ServletOutputStream}를 전달하는 구현.
 * <p>
 * * <p>
 * {@link MockHttpServletResponse}에서 사용함; 일반적으로 testing application controller에서 직접적으로 쓰지 않음.
 * </p>
 *
 * @author Juergen Hoeller
 * @see MockHttpServletResponse
 * @since 1.0.2
 */
public class DelegatingServletOutputStream extends ServletOutputStream {

    private final OutputStream targetStream;


    /**
     * 주어진 OutputStream으로 DelegatingServletOutputStream 생성함.
     * Create a DelegatingServletOutputStream for the given target stream.
     *
     * @param targetStream target stream(절대 {@code null}이면 안됨).
     */
    public DelegatingServletOutputStream(OutputStream targetStream) {
        Assert.notNull(targetStream, "Target OutputStream must not be null");
        this.targetStream = targetStream;
    }

    /**
     * target stream를 리턴함(절대 {@code null}이면 안됨).
     */
    public final OutputStream getTargetStream() {
        return this.targetStream;
    }


    @Override
    public void write(int b) throws IOException {
        this.targetStream.write(b);
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        this.targetStream.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.targetStream.close();
    }

}
