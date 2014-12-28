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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * {@link javax.servlet.jsp.JspWriter} 클래스의 mock 구현.
 * <p>
 * <p>
 * web framework테스트에 사용함; 또한 커스텀 jsp 태그 터플리케이션 테스트에 유용함.
 * <p>
 * </p>
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class MockJspWriter extends JspWriter {

    private final HttpServletResponse response;

    private PrintWriter targetWriter;


    /**
     * 주어진 response로 MockJspWriter를 생성함.
     * response의 기본 Writer를 사용함.
     *
     * @param response 감쌀 servlet response
     */
    public MockJspWriter(HttpServletResponse response) {
        this(response, null);
    }

    /**
     * 주어진 plain Writer로 MockJspWriter를 생성함.
     *
     * @param targetWriter 감쌀 target Writer
     */
    public MockJspWriter(Writer targetWriter) {
        this(null, targetWriter);
    }

    /**
     * 주어진 response로 MockJspWriter를 생성함.
     *
     * @param response 감쌀 servlet response
     * @param targetWriter 감쌀 target Writer
     */
    public MockJspWriter(HttpServletResponse response, Writer targetWriter) {
        super(DEFAULT_BUFFER, true);
        this.response = (response != null ? response : new MockHttpServletResponse());
        if (targetWriter instanceof PrintWriter) {
            this.targetWriter = (PrintWriter) targetWriter;
        } else if (targetWriter != null) {
            this.targetWriter = new PrintWriter(targetWriter);
        }
    }

    /**
     * target Writer를 늦게 초기화함.
     */
    protected PrintWriter getTargetWriter() throws IOException {
        if (this.targetWriter == null) {
            this.targetWriter = this.response.getWriter();
        }
        return this.targetWriter;
    }


    @Override
    public void clear() throws IOException {
        if (this.response.isCommitted()) {
            throw new IOException("Response already committed");
        }
        this.response.resetBuffer();
    }

    @Override
    public void clearBuffer() throws IOException {
    }

    @Override
    public void flush() throws IOException {
        this.response.flushBuffer();
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    @Override
    public int getRemaining() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void newLine() throws IOException {
        getTargetWriter().println();
    }

    @Override
    public void write(char value[], int offset, int length) throws IOException {
        getTargetWriter().write(value, offset, length);
    }

    @Override
    public void print(boolean value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(char value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(char[] value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(double value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(float value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(int value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(long value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(Object value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void print(String value) throws IOException {
        getTargetWriter().print(value);
    }

    @Override
    public void println() throws IOException {
        getTargetWriter().println();
    }

    @Override
    public void println(boolean value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(char value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(char[] value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(double value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(float value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(int value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(long value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(Object value) throws IOException {
        getTargetWriter().println(value);
    }

    @Override
    public void println(String value) throws IOException {
        getTargetWriter().println(value);
    }

}
