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
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.security.Principal;
import java.util.*;

/**
 * {@link javax.servlet.http.HttpServletRequest} 인터페이스의 mock 구현.
 * <p>
 * <p>
 * 이 request로 mock으로 감싸진 서버의 기본 {@link Locale}은 {@link Locale#ENGLISH}임.
 * 이값은 {@link #addPreferredLocale}나 {@link #setPreferredLocales}에 의해 변경될수 있음.
 * </p>
 * <p>
 * <p>
 * <p>Spring Framework 4.0에서, 이 클래스는 Servlet 3.0으로 디자인됨.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Rick Evans
 * @author Mark Fisher
 * @author Chris Beams
 * @author Sam Brannen
 * @since 1.0.2
 */
public class MockHttpServletRequest implements HttpServletRequest {

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    /**
     * 기본 프로토콜: 'http'.
     */
    public static final String DEFAULT_PROTOCOL = HTTP;

    /**
     * 기본 서버 주소: '127.0.0.1'.
     */
    public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";

    /**
     * 기본 서버 이름: 'localhost'.
     */
    public static final String DEFAULT_SERVER_NAME = "localhost";

    /**
     * 기본 서버 포트: '80'.
     */
    public static final int DEFAULT_SERVER_PORT = 80;

    /**
     * 기본 remote address: '127.0.0.1'.
     */
    public static final String DEFAULT_REMOTE_ADDR = "127.0.0.1";

    /**
     * 기본 remote host: 'localhost'.
     */
    public static final String DEFAULT_REMOTE_HOST = "localhost";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String HOST_HEADER = "Host";

    private static final String CHARSET_PREFIX = "charset=";

    private static final ServletInputStream EMPTY_SERVLET_INPUT_STREAM =
            new DelegatingServletInputStream(new ByteArrayInputStream(new byte[0]));


    private boolean active = true;


    // ---------------------------------------------------------------------
    // ServletRequest 프로퍼티들
    // ---------------------------------------------------------------------

    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    private String characterEncoding;

    private byte[] content;

    private String contentType;

    private final Map<String, String[]> parameters = new LinkedHashMap<String, String[]>(16);

    private String protocol = DEFAULT_PROTOCOL;

    private String scheme = DEFAULT_PROTOCOL;

    private String serverName = DEFAULT_SERVER_NAME;

    private int serverPort = DEFAULT_SERVER_PORT;

    private String remoteAddr = DEFAULT_REMOTE_ADDR;

    private String remoteHost = DEFAULT_REMOTE_HOST;

    /**
     * List of locales in descending order
     */
    private final List<Locale> locales = new LinkedList<Locale>();

    private boolean secure = false;

    private final ServletContext servletContext;

    private int remotePort = DEFAULT_SERVER_PORT;

    private String localName = DEFAULT_SERVER_NAME;

    private String localAddr = DEFAULT_SERVER_ADDR;

    private int localPort = DEFAULT_SERVER_PORT;

    private boolean asyncStarted = false;

    private boolean asyncSupported = false;

    private MockAsyncContext asyncContext;

    private DispatcherType dispatcherType = DispatcherType.REQUEST;


    // ---------------------------------------------------------------------
    // HttpServletRequest properties
    // ---------------------------------------------------------------------

    private String authType;

    private Cookie[] cookies;

    private final Map<String, HeaderValueHolder> headers = new LinkedCaseInsensitiveMap<HeaderValueHolder>();

    private String method;

    private String pathInfo;

    private String contextPath = "";

    private String queryString;

    private String remoteUser;

    private final Set<String> userRoles = new HashSet<String>();

    private Principal userPrincipal;

    private String requestedSessionId;

    private String requestURI;

    private String servletPath = "";

    private HttpSession session;

    private boolean requestedSessionIdValid = true;

    private boolean requestedSessionIdFromCookie = true;

    private boolean requestedSessionIdFromURL = false;

    private final Map<String, Part> parts = new LinkedHashMap<String, Part>();


    // ---------------------------------------------------------------------
    // 생성자들
    // ---------------------------------------------------------------------

    /**
     * 새 {@code MockHttpServletRequest}를 기본 {@link MockServletContext}로 생성.
     *
     * @see #MockHttpServletRequest(ServletContext, String, String)
     */
    public MockHttpServletRequest() {
        this(null, "", "");
    }

    /**
     * 새 {@code MockHttpServletRequest}를 기본 {@link MockServletContext}로 생성.
     *
     * @param method     request method ({@code null} 가능)
     * @param requestURI request URI ({@code null} 가능)
     * @see #setMethod
     * @see #setRequestURI
     * @see #MockHttpServletRequest(ServletContext, String, String)
     */
    public MockHttpServletRequest(String method, String requestURI) {
        this(null, method, requestURI);
    }

    /**
     * 새 {@code MockHttpServletRequest}를 주어진 {@link ServletContext}로 생성.
     *
     * @param servletContext request가 실행될 ServletContext 지정
     *                       ({@code null} 지정할 경우 기분 {@link MockServletContext} 사용)
     * @see #MockHttpServletRequest(ServletContext, String, String)
     */
    public MockHttpServletRequest(ServletContext servletContext) {
        this(servletContext, "", "");
    }

    /**
     * 새 {@code MockHttpServletRequest}를 주어진 {@link ServletContext}, {@code method}, {@code requestURI}로 생성.
     * <p>기본 {@link Locale}은 {@link Locale#ENGLISH}로 설정됨.
     *
     * @param servletContext request가 실행될 ServletContext 지정
     *                       ({@code null} 지정할 경우 기분 {@link MockServletContext} 사용)
     * @param method         request method ({@code null} 가능)
     * @param requestURI     request URI ({@code null} 가능)
     * @see #setMethod
     * @see #setRequestURI
     * @see #setPreferredLocales
     * @see MockServletContext
     */
    public MockHttpServletRequest(ServletContext servletContext, String method, String requestURI) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.method = method;
        this.requestURI = requestURI;
        this.locales.add(Locale.ENGLISH);
    }


    // ---------------------------------------------------------------------
    // Lifecycle 메서드들
    // ---------------------------------------------------------------------

    /**
     * 이 request와 연관된 ServletContext를 리턴.
     * (무슨 이유에선지 표준 HttpServletRequest에서는 사용가능하지 않음)
     */
    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    /**
     * 이 request가 활성화 상태인지 리턴(즉, 아직 완료되지 않았는지 리턴)
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * 이 request가 완료된것을 표시. state를 저장.
     */
    public void close() {
        this.active = false;
    }

    /**
     * 이 request를 무효화함, state를 claer.
     */
    public void invalidate() {
        close();
        clearAttributes();
    }

    /**
     * 이 request가 활성화 상태인지 체크(즉, 아직 완료되지 않았는지 체크)
     * 만약 활성화가 아니면 IllegalStateException를 발생.
     */
    protected void checkActive() throws IllegalStateException {
        if (!this.active) {
            throw new IllegalStateException("Request is not active anymore");
        }
    }


    // ---------------------------------------------------------------------
    // ServletRequest interface
    // ---------------------------------------------------------------------

    @Override
    public Object getAttribute(String name) {
        checkActive();
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkActive();
        return Collections.enumeration(new LinkedHashSet<String>(this.attributes.keySet()));
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
        updateContentTypeHeader();
    }

    private void updateContentTypeHeader() {
        if (StringUtils.hasLength(this.contentType)) {
            StringBuilder sb = new StringBuilder(this.contentType);
            if (!this.contentType.toLowerCase().contains(CHARSET_PREFIX) &&
                    StringUtils.hasLength(this.characterEncoding)) {
                sb.append(";").append(CHARSET_PREFIX).append(this.characterEncoding);
            }
            doAddHeaderValue(CONTENT_TYPE_HEADER, sb.toString(), true);
        }
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public int getContentLength() {
        return (this.content != null ? this.content.length : -1);
    }

    public long getContentLengthLong() {
        return getContentLength();
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        if (contentType != null) {
            int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
            if (charsetIndex != -1) {
                this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
            }
            updateContentTypeHeader();
        }
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletInputStream getInputStream() {
        if (this.content != null) {
            return new DelegatingServletInputStream(new ByteArrayInputStream(this.content));
        } else {
            return EMPTY_SERVLET_INPUT_STREAM;
        }
    }

    /**
     * HTTP 파라미터를 set함.
     * <p>만약 이미 존재하는 값이 있으면 replace됨.
     */
    public void setParameter(String name, String value) {
        setParameter(name, new String[]{value});
    }

    /**
     * HTTP 파라미터를 set함.
     * <p>만약 이미 존재하는 값이 있으면 replace됨.
     */
    public void setParameter(String name, String[] values) {
        Assert.notNull(name, "Parameter name must not be null");
        this.parameters.put(name, values);
    }

    /**
     * 전체 파라미터를 Map에서 입력된 값으로 replace
     * 만약 기존값을 유지하고 값을 추가 하고 싶으면 {@link #addParameters(java.util.Map)}를 사용.
     */
    @SuppressWarnings("rawtypes")
    public void setParameters(Map params) {
        Assert.notNull(params, "Parameter map must not be null");
        for (Object key : params.keySet()) {
            Assert.isInstanceOf(String.class, key,
                    "Parameter map key must be of type [" + String.class.getName() + "]");
            Object value = params.get(key);
            if (value instanceof String) {
                this.setParameter((String) key, (String) value);
            } else if (value instanceof String[]) {
                this.setParameter((String) key, (String[]) value);
            } else {
                throw new IllegalArgumentException(
                        "Parameter map value must be single value " + " or array of type [" + String.class.getName() + "]");
            }
        }
    }

    /**
     * HTTP 파라미터를 추가.
     * <p>기존에 존재하는 값을 그대로 두고, 새롭게 파라미터를 추가함.
     */
    public void addParameter(String name, String value) {
        addParameter(name, new String[]{value});
    }

    /**
     * HTTP 파라미터를 추가.
     * <p>기존에 존재하는 값을 그대로 두고, 새롭게 파라미터를 추가함.
     */
    public void addParameter(String name, String[] values) {
        Assert.notNull(name, "Parameter name must not be null");
        String[] oldArr = this.parameters.get(name);
        if (oldArr != null) {
            String[] newArr = new String[oldArr.length + values.length];
            System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
            System.arraycopy(values, 0, newArr, oldArr.length, values.length);
            this.parameters.put(name, newArr);
        } else {
            this.parameters.put(name, values);
        }
    }

    /**
     * 기존에 값을 그대로 유지하며 Map을 파라미터로 추가. <br />
     * 기존값을 새로운 Map으로 replace하고 싶으면{@link #setParameters(java.util.Map)}를 사용하면 됨.
     */
    @SuppressWarnings("rawtypes")
    public void addParameters(Map params) {
        Assert.notNull(params, "Parameter map must not be null");
        for (Object key : params.keySet()) {
            Assert.isInstanceOf(String.class, key,
                    "Parameter map key must be of type [" + String.class.getName() + "]");
            Object value = params.get(key);
            if (value instanceof String) {
                this.addParameter((String) key, (String) value);
            } else if (value instanceof String[]) {
                this.addParameter((String) key, (String[]) value);
            } else {
                throw new IllegalArgumentException("Parameter map value must be single value " +
                        " or array of type [" + String.class.getName() + "]");
            }
        }
    }

    /**
     * 지정한 HTTP parameter를 제거
     */
    public void removeParameter(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        this.parameters.remove(name);
    }

    /**
     * 모든 파라미터를 제거
     */
    public void removeAllParameters() {
        this.parameters.clear();
    }

    @Override
    public String getParameter(String name) {
        String[] arr = (name != null ? this.parameters.get(name) : null);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return (name != null ? this.parameters.get(name) : null);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(this.parameters);
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String getScheme() {
        return this.scheme;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public String getServerName() {
        String host = getHeader(HOST_HEADER);
        if (host != null) {
            host = host.trim();
            if (host.startsWith("[")) {
                host = host.substring(1, host.indexOf(']'));
            } else if (host.contains(":")) {
                host = host.substring(0, host.indexOf(':'));
            }
            return host;
        }

        // else
        return this.serverName;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public int getServerPort() {
        String host = getHeader(HOST_HEADER);
        if (host != null) {
            host = host.trim();
            int idx;
            if (host.startsWith("[")) {
                idx = host.indexOf(':', host.indexOf(']'));
            } else {
                idx = host.indexOf(':');
            }
            if (idx != -1) {
                return Integer.parseInt(host.substring(idx + 1));
            }
        }

        // else
        return this.serverPort;
    }

    @Override
    public BufferedReader getReader() throws UnsupportedEncodingException {
        if (this.content != null) {
            InputStream sourceStream = new ByteArrayInputStream(this.content);
            Reader sourceReader = (this.characterEncoding != null) ?
                    new InputStreamReader(sourceStream, this.characterEncoding) : new InputStreamReader(sourceStream);
            return new BufferedReader(sourceReader);
        } else {
            return null;
        }
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public String getRemoteAddr() {
        return this.remoteAddr;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public String getRemoteHost() {
        return this.remoteHost;
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkActive();
        Assert.notNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        checkActive();
        Assert.notNull(name, "Attribute name must not be null");
        this.attributes.remove(name);
    }

    /**
     * 모든 request attribute를 clear.
     */
    public void clearAttributes() {
        this.attributes.clear();
    }

    /**
     * 기존 locale에 우선하는 선호하는 locale을 지정.
     *
     * @see #setPreferredLocales
     */
    public void addPreferredLocale(Locale locale) {
        Assert.notNull(locale, "Locale must not be null");
        this.locales.add(0, locale);
    }

    /**
     * 선호하는 Locale 리스트를 내림차순으로 set, 효과적으로 기존 locale들을 replace하는 방법.
     *
     * @see #addPreferredLocale
     * @since 3.2
     */
    public void setPreferredLocales(List<Locale> locales) {
        Assert.notEmpty(locales, "Locale list must not be empty");
        this.locales.clear();
        this.locales.addAll(locales);
    }

    /**
     * {@linkplain Locale locale}을 리턴
     * <p>기본 {@link Locale}은 {@link Locale#ENGLISH}로 설정됨.
     * <p>
     * <p>Servlet 명세와 달리, 이 mock 구현은 {@code Accept-Language} 헤더에 기재된 locale을 고려하지 않음.
     *
     * @see javax.servlet.ServletRequest#getLocale()
     * @see #addPreferredLocale(Locale)
     * @see #setPreferredLocales(List)
     */
    @Override
    public Locale getLocale() {
        return this.locales.get(0);
    }

    /**
     * {@linkplain Locale locale}을 리턴
     * <p>기본 {@link Locale}은 {@link Locale#ENGLISH}로 설정됨.
     * <p>
     * <p>Servlet 명세와 달리, 이 mock 구현은 {@code Accept-Language} 헤더에 기재된 locale을 고려하지 않음.
     *
     * @see javax.servlet.ServletRequest#getLocales()
     * @see #addPreferredLocale(Locale)
     * @see #setPreferredLocales(List)
     */
    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(this.locales);
    }

    /**
     * {@code secure} boolean 플래그 설정. mock request가 HTTPS인지 알림.
     *
     * @see #isSecure()
     * @see #getScheme()
     * @see #setScheme(String)
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * {@link #setSecure secure}가 true로 설정되어 있거나 {@link #getScheme scheme}가 @code 이면 {@code true}를 리턴
     *
     * @see javax.servlet.ServletRequest#isSecure()
     */
    @Override
    public boolean isSecure() {
        return (this.secure || HTTPS.equalsIgnoreCase(this.scheme));
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return new MockRequestDispatcher(path);
    }

    @Override
    @Deprecated
    public String getRealPath(String path) {
        return this.servletContext.getRealPath(path);
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public int getRemotePort() {
        return this.remotePort;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public String getLocalName() {
        return this.localName;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    @Override
    public String getLocalAddr() {
        return this.localAddr;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    @Override
    public int getLocalPort() {
        return this.localPort;
    }

    @Override
    public AsyncContext startAsync() {
        return startAsync(this, null);
    }

    @Override
    public AsyncContext startAsync(ServletRequest request, ServletResponse response) {
        if (!this.asyncSupported) {
            throw new IllegalStateException("Async not supported");
        }
        this.asyncStarted = true;
        this.asyncContext = new MockAsyncContext(request, response);
        return this.asyncContext;
    }

    public void setAsyncStarted(boolean asyncStarted) {
        this.asyncStarted = asyncStarted;
    }

    @Override
    public boolean isAsyncStarted() {
        return this.asyncStarted;
    }

    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    @Override
    public boolean isAsyncSupported() {
        return this.asyncSupported;
    }

    public void setAsyncContext(MockAsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return this.asyncContext;
    }

    public void setDispatcherType(DispatcherType dispatcherType) {
        this.dispatcherType = dispatcherType;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return this.dispatcherType;
    }


    // ---------------------------------------------------------------------
    // HttpServletRequest 인터페이스
    // ---------------------------------------------------------------------

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    @Override
    public String getAuthType() {
        return this.authType;
    }

    public void setCookies(Cookie... cookies) {
        this.cookies = cookies;
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    /**
     * 주어진 이름으로 헤더 추가.
     * <p>
     * <p>
     * 만약 주어진 이름으로 값이 존재하지 않으면, value를 as-is로 사용함.
     * 만약 기존에 존재한 값이라면, 문자열 배열을 생성하여 주어진 값을 뒤에 추가
     * </p>
     * <p>
     * <p>
     * Servelt spec (see {@code getHeaders} accessor)에 따라 Multiple value는 문자열 리스트로 저장가능함.
     * {@code addHeader}의 반복 호출 대신에, 전체 배열이나 값들의  Collection을 파라미터로 추가 가능함.
     *
     * @see #getHeaderNames
     * @see #getHeader
     * @see #getHeaders
     * @see #getDateHeader
     * @see #getIntHeader
     */
    public void addHeader(String name, Object value) {
        if (CONTENT_TYPE_HEADER.equalsIgnoreCase(name)) {
            setContentType((String) value);
            return;
        }
        doAddHeaderValue(name, value, false);
    }

    @SuppressWarnings("rawtypes")
    private void doAddHeaderValue(String name, Object value, boolean replace) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        Assert.notNull(value, "Header value must not be null");
        if (header == null || replace) {
            header = new HeaderValueHolder();
            this.headers.put(name, header);
        }
        if (value instanceof Collection) {
            header.addValues((Collection) value);
        } else if (value.getClass().isArray()) {
            header.addValueArray(value);
        } else {
            header.addValue(value);
        }
    }

    @Override
    public long getDateHeader(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        Object value = (header != null ? header.getValue() : null);
        if (value instanceof Date) {
            return ((Date) value).getTime();
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value != null) {
            throw new IllegalArgumentException(
                    "Value for header '" + name + "' is neither a Date nor a Number: " + value);
        } else {
            return -1L;
        }
    }

    @Override
    public String getHeader(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        return (header != null ? header.getStringValue() : null);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        return Collections.enumeration(header != null ? header.getStringValues() : new LinkedList<String>());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headers.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        Object value = (header != null ? header.getValue() : null);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else if (value != null) {
            throw new NumberFormatException("Value for header '" + name + "' is not a Number: " + value);
        } else {
            return -1;
        }
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public String getPathInfo() {
        return this.pathInfo;
    }

    @Override
    public String getPathTranslated() {
        return (this.pathInfo != null ? getRealPath(this.pathInfo) : null);
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    @Override
    public String getRemoteUser() {
        return this.remoteUser;
    }

    public void addUserRole(String role) {
        this.userRoles.add(role);
    }

    @Override
    public boolean isUserInRole(String role) {
        return (this.userRoles.contains(role) || (this.servletContext instanceof MockServletContext &&
                ((MockServletContext) this.servletContext).getDeclaredRoles().contains(role)));
    }

    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Principal getUserPrincipal() {
        return this.userPrincipal;
    }

    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    @Override
    public String getRequestedSessionId() {
        return this.requestedSessionId;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public String getRequestURI() {
        return this.requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer(this.scheme).append("://").append(this.serverName);

        if (this.serverPort > 0 && ((HTTP.equalsIgnoreCase(this.scheme) && this.serverPort != 80) ||
                (HTTPS.equalsIgnoreCase(this.scheme) && this.serverPort != 443))) {
            url.append(':').append(this.serverPort);
        }

        if (StringUtils.hasText(getRequestURI())) {
            url.append(getRequestURI());
        }

        return url;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public String getServletPath() {
        return this.servletPath;
    }

    public void setSession(HttpSession session) {
        this.session = session;
        if (session instanceof MockHttpSession) {
            MockHttpSession mockSession = ((MockHttpSession) session);
            mockSession.access();
        }
    }

    @Override
    public HttpSession getSession(boolean create) {
        checkActive();
        // 무효화 되었으면 reset
        if (this.session instanceof MockHttpSession && ((MockHttpSession) this.session).isInvalid()) {
            this.session = null;
        }
        // 필요시 새 세션을 생성
        if (this.session == null && create) {
            this.session = new MockHttpSession(this.servletContext);
        }
        return this.session;
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    /**
     * 만약 세션이 mock 세션이면 메서드 호출의 구현 {@link MockHttpSession#changeSessionId()}.
     * 그게 아니면 단순히 현재 세션 id를 리턴.
     *
     * @since 4.0.3
     */
    public String changeSessionId() {
        Assert.isTrue(this.session != null, "The request does not have a session");
        if (this.session instanceof MockHttpSession) {
            return ((MockHttpSession) session).changeSessionId();
        }
        return this.session.getId();
    }

    public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
        this.requestedSessionIdValid = requestedSessionIdValid;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return this.requestedSessionIdValid;
    }

    public void setRequestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
        this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this.requestedSessionIdFromCookie;
    }

    public void setRequestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
        this.requestedSessionIdFromURL = requestedSessionIdFromURL;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return this.requestedSessionIdFromURL;
    }

    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws ServletException {
        this.userPrincipal = null;
        this.remoteUser = null;
        this.authType = null;
    }

    public void addPart(Part part) {
        this.parts.put(part.getName(), part);
    }

    @Override
    public Part getPart(String name) throws IOException, IllegalStateException, ServletException {
        return this.parts.get(name);
    }

    @Override
    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
        return this.parts.values();
    }

}
