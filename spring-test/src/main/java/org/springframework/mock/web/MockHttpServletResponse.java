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

package org.springframework.mock.web;

import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * {@link javax.servlet.http.HttpServletResponse} 인터페이스의 mock 구현.
 * <p>
 * <p>
 * Spring Framework 4.0에서, 이 클래스는 Servlet 3.0으로 디자인됨.
 * </p>
 * <p>
 * 그밖에, {@code MockHttpServletResponse}는 또한 Servelt 3.1 스펙의 {@code setContentLengthLong()}와 호환된다.
 * </p>
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 1.0.2
 */
public class MockHttpServletResponse implements HttpServletResponse {

    private static final String CHARSET_PREFIX = "charset=";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String CONTENT_LENGTH_HEADER = "Content-Length";

    private static final String LOCATION_HEADER = "Location";


    //---------------------------------------------------------------------
    // ServletResponse 프로퍼티들
    //---------------------------------------------------------------------

    private boolean outputStreamAccessAllowed = true;

    private boolean writerAccessAllowed = true;

    private String characterEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;

    private boolean charset = false;

    private final ByteArrayOutputStream content = new ByteArrayOutputStream(1024);

    private final ServletOutputStream outputStream = new ResponseServletOutputStream(this.content);

    private PrintWriter writer;

    private long contentLength = 0;

    private String contentType;

    private int bufferSize = 4096;

    private boolean committed;

    private Locale locale = Locale.getDefault();


    //---------------------------------------------------------------------
    // HttpServletResponse 프로퍼티들
    //---------------------------------------------------------------------

    private final List<Cookie> cookies = new ArrayList<Cookie>();

    private final Map<String, HeaderValueHolder> headers = new LinkedCaseInsensitiveMap<HeaderValueHolder>();

    private int status = HttpServletResponse.SC_OK;

    private String errorMessage;

    private String forwardedUrl;

    private final List<String> includedUrls = new ArrayList<String>();


    //---------------------------------------------------------------------
    // ServletResponse 인터페이스
    //---------------------------------------------------------------------

    /**
     * {@link #getOutputStream()} access를 허용할지 설정.
     * <p>기본값은 {@code true}.
     */
    public void setOutputStreamAccessAllowed(boolean outputStreamAccessAllowed) {
        this.outputStreamAccessAllowed = outputStreamAccessAllowed;
    }

    /**
     * {@link #getOutputStream()} access 허용여부를 리턴.
     */
    public boolean isOutputStreamAccessAllowed() {
        return this.outputStreamAccessAllowed;
    }

    /**
     * {@link #getWriter()} access를 허용할지 설정.
     * <p>기본값은 {@code true}.
     */
    public void setWriterAccessAllowed(boolean writerAccessAllowed) {
        this.writerAccessAllowed = writerAccessAllowed;
    }

    /**
     * {@link #getOutputStream()} access 허용여부를 리턴.
     */
    public boolean isWriterAccessAllowed() {
        return this.writerAccessAllowed;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
        this.charset = true;
        updateContentTypeHeader();
    }

    private void updateContentTypeHeader() {
        if (this.contentType != null) {
            StringBuilder sb = new StringBuilder(this.contentType);
            if (!this.contentType.toLowerCase().contains(CHARSET_PREFIX) && this.charset) {
                sb.append(";").append(CHARSET_PREFIX).append(this.characterEncoding);
            }
            doAddHeaderValue(CONTENT_TYPE_HEADER, sb.toString(), true);
        }
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        if (!this.outputStreamAccessAllowed) {
            throw new IllegalStateException("OutputStream access not allowed");
        }
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        if (!this.writerAccessAllowed) {
            throw new IllegalStateException("Writer access not allowed");
        }
        if (this.writer == null) {
            Writer targetWriter = (this.characterEncoding != null ?
                    new OutputStreamWriter(this.content, this.characterEncoding) : new OutputStreamWriter(this.content));
            this.writer = new ResponsePrintWriter(targetWriter);
        }
        return this.writer;
    }

    public byte[] getContentAsByteArray() {
        flushBuffer();
        return this.content.toByteArray();
    }

    public String getContentAsString() throws UnsupportedEncodingException {
        flushBuffer();
        return (this.characterEncoding != null ?
                this.content.toString(this.characterEncoding) : this.content.toString());
    }

    @Override
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
        doAddHeaderValue(CONTENT_LENGTH_HEADER, contentLength, true);
    }

    public int getContentLength() {
        return (int) this.contentLength;
    }

    public void setContentLengthLong(long contentLength) {
        this.contentLength = contentLength;
        doAddHeaderValue(CONTENT_LENGTH_HEADER, contentLength, true);
    }

    public long getContentLengthLong() {
        return this.contentLength;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
        if (contentType != null) {
            int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
            if (charsetIndex != -1) {
                this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
                this.charset = true;
            }
            updateContentTypeHeader();
        }
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public int getBufferSize() {
        return this.bufferSize;
    }

    @Override
    public void flushBuffer() {
        setCommitted(true);
    }

    @Override
    public void resetBuffer() {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot reset buffer - response is already committed");
        }
        this.content.reset();
    }

    private void setCommittedIfBufferSizeExceeded() {
        int bufSize = getBufferSize();
        if (bufSize > 0 && this.content.size() > bufSize) {
            setCommitted(true);
        }
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    @Override
    public boolean isCommitted() {
        return this.committed;
    }

    @Override
    public void reset() {
        resetBuffer();
        this.characterEncoding = null;
        this.contentLength = 0;
        this.contentType = null;
        this.locale = null;
        this.cookies.clear();
        this.headers.clear();
        this.status = HttpServletResponse.SC_OK;
        this.errorMessage = null;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }


    //---------------------------------------------------------------------
    // HttpServletResponse 인터페이스
    //---------------------------------------------------------------------

    @Override
    public void addCookie(Cookie cookie) {
        Assert.notNull(cookie, "Cookie must not be null");
        this.cookies.add(cookie);
    }

    public Cookie[] getCookies() {
        return this.cookies.toArray(new Cookie[this.cookies.size()]);
    }

    public Cookie getCookie(String name) {
        Assert.notNull(name, "Cookie name must not be null");
        for (Cookie cookie : this.cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    @Override
    public boolean containsHeader(String name) {
        return (HeaderValueHolder.getByName(this.headers, name) != null);
    }

    /**
     * 정의 된 모든 헤더들을 문자열 집합으로 리턴함.
     * <p>
     * Servlet 3.0에서는, 이 메서드가 HttpServletResponse에도 정의되어 있음.
     * </p>
     *
     * @return 정의 된 모든 헤더들을 문자열 {@code Set}이나 빈 {@code Set}
     */
    @Override
    public Collection<String> getHeaderNames() {
        return this.headers.keySet();
    }

    /**
     * <p>
     * 주어진 이름의 헤더 값중 첫번째 값을 리턴함.
     * 스프링 3.1 이후, Servlet 3.0 호환성을 위해 문자열로 형 변환된 값들의 리스트를 리턴.
     * 형봔환이 되지 않은 raw Object access를 위해서는 {@link #getHeaderValues(String)} 사용을 고려해 볼것.
     * </p>
     *
     * @param name 헤더 이름
     * @return 연관된 헤더 값들이나 (관련 헤더값이 없으면) 빈 리스트
     */
    @Override
    public String getHeader(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        return (header != null ? header.getStringValue() : null);
    }

    /**
     * <p>
     * 주어진 이름의 헤더 리스트를 리턴.
     * 스프링 3.1 이후, Servlet 3.0 호환성을 위해 문자열로 형 변환된 값들의 리스트를 리턴.
     * 형봔환이 되지 않은 raw Object access를 위해서는 {@link #getHeaderValues(String)} 사용을 고려해 볼것.
     * </p>
     *
     * @param name 헤더 이름
     * @return 연관된 헤더 값들이나 (관련 헤더값이 없으면) 빈 리스트
     */
    @Override
    public List<String> getHeaders(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        if (header != null) {
            return header.getStringValues();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 주어진 이름의 헤더 중에서 첫번째 값을 리턴함.
     *
     * @param name 헤더 이름
     * @return 연관된 헤더 값들이나 (관련 헤더값이 없으면) 빈 리스트
     */
    public Object getHeaderValue(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        return (header != null ? header.getValue() : null);
    }

    /**
     * 주어진 이름의 헤더를 {@code List<Object>}로 리턴
     *
     * @param name 헤더 이름
     * @return 연관된 헤더 값들이나 (관련 헤더값이 없으면) 빈 리스트
     */
    public List<Object> getHeaderValues(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        if (header != null) {
            return header.getValues();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 기본 구현은 주어진 url 그대로 리턴함.
     * <p>
     * sub 클래스에서 override하거나 session id를 추가하거나 그외에 것들을 할 수 있음.
     * </p>
     */
    @Override
    public String encodeURL(String url) {
        return url;
    }

    /**
     * 기본 구현은 {@link #encodeURL}로 전달함.
     * <p>
     * sub 클래스에서 override하거나 session id를 추가하거나 그외에 것들을 할 수 있음.
     * 일반적인 URL 인코딩 규칙을 만드려면, 공통 {@link #encodeURL} 메서드를 override 해야함.
     * 그 목적은 일반 URL들과 마찬가지로 redirect URL에도 같이 적용하기 위해서임.
     * (역자주: encodeRedirectURL()도 내부적으로 encodeURL()을 사용하니 encodeURL()만 재정의하면
     * 인코딩 규칙이 만들어 진다는 얘기)
     * </p>
     * <p>
     * The default implementation delegates to {@link #encodeURL},
     * returning the given URL String as-is.
     * <p>Can be overridden in subclasses, appending a session id or the like
     * in a redirect-specific fashion. For general URL encoding rules,
     * override the common {@link #encodeURL} method instead, applying
     * to redirect URLs as well as to general URLs.
     */
    @Override
    public String encodeRedirectURL(String url) {
        return encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    @Override
    public void sendError(int status, String errorMessage) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot set error status - response is already committed");
        }
        this.status = status;
        this.errorMessage = errorMessage;
        setCommitted(true);
    }

    @Override
    public void sendError(int status) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot set error status - response is already committed");
        }
        this.status = status;
        setCommitted(true);
    }

    @Override
    public void sendRedirect(String url) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Cannot send redirect - response is already committed");
        }
        Assert.notNull(url, "Redirect URL must not be null");
        setHeader(LOCATION_HEADER, url);
        setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        setCommitted(true);
    }

    public String getRedirectedUrl() {
        return getHeader(LOCATION_HEADER);
    }

    @Override
    public void setDateHeader(String name, long value) {
        setHeaderValue(name, value);
    }

    @Override
    public void addDateHeader(String name, long value) {
        addHeaderValue(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        setHeaderValue(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        addHeaderValue(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        setHeaderValue(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        addHeaderValue(name, value);
    }

    private void setHeaderValue(String name, Object value) {
        if (setSpecialHeader(name, value)) {
            return;
        }
        doAddHeaderValue(name, value, true);
    }

    private void addHeaderValue(String name, Object value) {
        if (setSpecialHeader(name, value)) {
            return;
        }
        doAddHeaderValue(name, value, false);
    }

    private boolean setSpecialHeader(String name, Object value) {
        if (CONTENT_TYPE_HEADER.equalsIgnoreCase(name)) {
            setContentType((String) value);
            return true;
        } else if (CONTENT_LENGTH_HEADER.equalsIgnoreCase(name)) {
            setContentLength(Integer.parseInt((String) value));
            return true;
        } else {
            return false;
        }
    }

    private void doAddHeaderValue(String name, Object value, boolean replace) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        Assert.notNull(value, "Header value must not be null");
        if (header == null) {
            header = new HeaderValueHolder();
            this.headers.put(name, header);
        }
        if (replace) {
            header.setValue(value);
        } else {
            header.addValue(value);
        }
    }

    @Override
    public void setStatus(int status) {
        if (!this.isCommitted()) {
            this.status = status;
        }
    }

    @Override
    @Deprecated
    public void setStatus(int status, String errorMessage) {
        if (!this.isCommitted()) {
            this.status = status;
            this.errorMessage = errorMessage;
        }
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }


    //---------------------------------------------------------------------
    // MockRequestDispatcher 메서드들
    //---------------------------------------------------------------------

    public void setForwardedUrl(String forwardedUrl) {
        this.forwardedUrl = forwardedUrl;
    }

    public String getForwardedUrl() {
        return this.forwardedUrl;
    }

    public void setIncludedUrl(String includedUrl) {
        this.includedUrls.clear();
        if (includedUrl != null) {
            this.includedUrls.add(includedUrl);
        }
    }

    public String getIncludedUrl() {
        int count = this.includedUrls.size();
        if (count > 1) {
            throw new IllegalStateException(
                    "More than 1 URL included - check getIncludedUrls instead: " + this.includedUrls);
        }
        return (count == 1 ? this.includedUrls.get(0) : null);
    }

    public void addIncludedUrl(String includedUrl) {
        Assert.notNull(includedUrl, "Included URL must not be null");
        this.includedUrls.add(includedUrl);
    }

    public List<String> getIncludedUrls() {
        return this.includedUrls;
    }


    /**
     * ServletOutputStream을 도입한 Inner class로써
     * 버퍼 사이즈가 초과될때 response에 커밋됨으로 표시하는 역할을 함.
     */
    private class ResponseServletOutputStream extends DelegatingServletOutputStream {

        public ResponseServletOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            setCommitted(true);
        }
    }


    /**
     * PrintWriter를 도입한 Inner class로써
     * 버퍼 사이즈가 초과될때 response에 커밋됨으로 표시하는 역할을 함.
     */
    private class ResponsePrintWriter extends PrintWriter {

        public ResponsePrintWriter(Writer out) {
            super(out, true);
        }

        @Override
        public void write(char buf[], int off, int len) {
            super.write(buf, off, len);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void write(String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void write(int c) {
            super.write(c);
            super.flush();
            setCommittedIfBufferSizeExceeded();
        }

        @Override
        public void flush() {
            super.flush();
            setCommitted(true);
        }
    }

}
