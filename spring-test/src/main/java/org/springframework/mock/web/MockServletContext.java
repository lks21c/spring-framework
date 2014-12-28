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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.WebUtils;

import javax.activation.FileTypeMap;
import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * {@link javax.servlet.ServletContext} 인터페이스의 mock 구현.
 * <p>
 * <p>Spring Framework 4.0에서, 이 클래스는 Servlet 3.0으로 디자인됨.
 * <p>
 * Servlet 3.0과 호환되지만 {@link #setMajorVersion}/{@link #setMinorVersion}으로 버전을 설정가능;기본값은 3.0
 * 노트 :  Servlet 3.0 지원은 제한적음: servlet, filter, listener registration 메서드들은 지원안됨;JSP 설정도 안됨.
 * 우리는 위에서 언급한 메서들이 사용될 가능성이 잇는 ServletContainerInitializers 와 WebApplicationInitializer 유닛 테스트를 추천하지 않음.
 * <p>
 * <p>
 * <p>web framework테스트에 사용함; 또한 어플리케이션 컨트롤러 테스트에 유용함.
 * 어플리케이션 컴포넌트가 명시적으로 {@code ServletContext}에 접근하지 않는한,
 * 테스팅을 위해 @code ClassPathXmlApplicationContext} 나
 * {@code FileSystemXmlApplicationContext}가 context 로드 용도로 쓰일수 있음.
 * 심지어 {@code DispatcherServlet} context 정의 용도로도 사용할 수 있음.
 * <p>
 * <p>
 * 테스트 환경에서 완전한 {@code WebApplicationContext}를 setup하기 위해 {@code MockServletContext} 인스턴스를 전달하여
 * {@code AnnotationConfigWebApplicationContext}나 {@code XmlWebApplicationContext}나 {@code GenericWebApplicationContext}을 사용 할 수 있음
 * </p>
 * 상대 파일 경로에서 resource path가 해석되는 것을 확인 하기 위해,
 * {@code FileSystemResourceLoader}와 함께 {@code MockServletContext}를 설정할수 있음.
 * <p>
 * <p>
 * 일반적인 setup은 JVM working 디렉토리를 web 어플리케이션의 디렉토리로 명시하고 파일 base의 리소스 loading을 하는것임.
 * 이것은 web 어플리케이션에서 쓰일 context 파일들을 상대경로로 읽어 들여 올바르게 해석되는것을 확인해줌.
 * 이러한 작업은 {@code FileSystemXmlApplicationContext}(파일에서 바로 읽음)와 {@code XmlWebApplicationContext}과
 * {@code MockServletContext}의 조합({@code FileSystemResourceLoader}를 가지고 {@code MockServletContext}이 설정되었을 경우)으로 가능함.
 * </p>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see #MockServletContext(org.springframework.core.io.ResourceLoader)
 * @see org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * @see org.springframework.web.context.support.XmlWebApplicationContext
 * @see org.springframework.web.context.support.GenericWebApplicationContext
 * @see org.springframework.context.support.ClassPathXmlApplicationContext
 * @see org.springframework.context.support.FileSystemXmlApplicationContext
 * @since 1.0.2
 */
public class MockServletContext implements ServletContext {

    /**
     * 톰캣, 제티, Jboss, GlassFish에서 쓰는 기본 Servlet 이름: {@value}.
     */
    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

    private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";

    private static final Set<SessionTrackingMode> DEFAULT_SESSION_TRACKING_MODES =
            new LinkedHashSet<SessionTrackingMode>(3);

    static {
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.COOKIE);
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.URL);
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.SSL);
    }


    private final Log logger = LogFactory.getLog(getClass());

    private final ResourceLoader resourceLoader;

    private final String resourceBasePath;

    private String contextPath = "";

    private final Map<String, ServletContext> contexts = new HashMap<String, ServletContext>();

    private int majorVersion = 3;

    private int minorVersion = 0;

    private int effectiveMajorVersion = 3;

    private int effectiveMinorVersion = 0;

    private final Map<String, RequestDispatcher> namedRequestDispatchers = new HashMap<String, RequestDispatcher>();

    private String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;

    private final Map<String, String> initParameters = new LinkedHashMap<String, String>();

    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    private String servletContextName = "MockServletContext";

    private final Set<String> declaredRoles = new HashSet<String>();

    private Set<SessionTrackingMode> sessionTrackingModes;

    private final SessionCookieConfig sessionCookieConfig = new MockSessionCookieConfig();


    /**
     * 새 {@code MockServletContext}를 생성함, base path와 {@link DefaultResourceLoader}없음
     * (i.e. the classpath root as WAR root).
     *
     * @see org.springframework.core.io.DefaultResourceLoader
     */
    public MockServletContext() {
        this("", null);
    }

    /**
     * 새 {@code MockServletContext}를 생성함, {@link DefaultResourceLoader}를 사용함.
     *
     * @param resourceBasePath WAR의 루트 디렉토리 (슬래쉬로 끝나면 안됨)
     * @see org.springframework.core.io.DefaultResourceLoader
     */
    public MockServletContext(String resourceBasePath) {
        this(resourceBasePath, null);
    }

    /**
     * 새 {@code MockServletContext}를 생성함, 명시된 {@link ResourceLoader}를 사용함, base path 없음.
     *
     * @param resourceLoader 사용할 ResourceLoader(기본값을 위해서는 {@code null}입력)
     */
    public MockServletContext(ResourceLoader resourceLoader) {
        this("", resourceLoader);
    }

    /**
     * 새 {@code MockServletContext}를 생성함, 지정된 resouce base path와 resource loader를 사용함.
     * <p>
     * <p>
     * {@literal 'default'}이름으로 {@link MockRequestDispatcher}를 Servlet으로 등록함.
     * </p>
     *
     * @param resourceBasePath WAR의 루트 디렉토리 (슬래쉬로 끝나면 안됨)
     * @param resourceLoader   사용할 ResourceLoader(기본값을 위해서는 {@code null}입력)
     * @see #registerNamedDispatcher
     */
    public MockServletContext(String resourceBasePath, ResourceLoader resourceLoader) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
        this.resourceBasePath = (resourceBasePath != null ? resourceBasePath : "");

        // JVM temp dir을 ServletContext의 temp dir로 사용함.
        String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
        if (tempDir != null) {
            this.attributes.put(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File(tempDir));
        }

        registerNamedDispatcher(this.defaultServletName, new MockRequestDispatcher(this.defaultServletName));
    }

    /**
     * 주어진 path의 full resouce path를 생성함, {@code MockServletContext}의 resouce base path를 앞에 덧붙임.
     *
     * @param path 명시된 path
     * @return full resource path
     */
    protected String getResourceLocation(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return this.resourceBasePath + path;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = (contextPath != null ? contextPath : "");
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    public void registerContext(String contextPath, ServletContext context) {
        this.contexts.put(contextPath, context);
    }

    @Override
    public ServletContext getContext(String contextPath) {
        if (this.contextPath.equals(contextPath)) {
            return this;
        }
        return this.contexts.get(contextPath);
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    @Override
    public int getMajorVersion() {
        return this.majorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    @Override
    public int getMinorVersion() {
        return this.minorVersion;
    }

    public void setEffectiveMajorVersion(int effectiveMajorVersion) {
        this.effectiveMajorVersion = effectiveMajorVersion;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return this.effectiveMajorVersion;
    }

    public void setEffectiveMinorVersion(int effectiveMinorVersion) {
        this.effectiveMinorVersion = effectiveMinorVersion;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return this.effectiveMinorVersion;
    }

    /**
     * 이 메서드는 MIME type을 해석하기 위해 Java Activation Framework의 {@link javax.activation.FileTypeMap#getDefaultFileTypeMap() FileTypeMap}를
     * 사용함.
     * MIME type을 해석할수 없으면 Java Activation Framework는 {@code "application/octet-stream"}을 리턴함.
     * 그러므로, {@link ServletContext#getMimeType(String)} 규칙을 준수하기 위해
     * <p>The Java Activation Framework returns {@code "application/octet-stream"}
     * if the MIME type is unknown (i.e., it never returns {@code null}). Thus, in
     * order to honor the {@link ServletContext#getMimeType(String)} contract,
     * MIME type이 {@code "application/octet-stream"}이면 {@code null}을 리턴함.
     * <p>
     * {@code MockServletContext}는 Custom MIME type을 설정할수 있는 직접적인 매커니즘 제공하지 않음;
     * 그러나, 기본 {@code FileTypeMap}이 {@code javax.activation.MimetypesFileTypeMap}의 인스턴스 이면,
     * Custom MIME type인 {@code text/enigma}이 {@code .puzzle}을 확장자로 아래와 같이 설정 가능함.
     * <p>
     * <pre style="code">
     * MimetypesFileTypeMap mimetypesFileTypeMap = (MimetypesFileTypeMap) FileTypeMap.getDefaultFileTypeMap();
     * mimetypesFileTypeMap.addMimeTypes("text/enigma    puzzle");
     * </pre>
     */
    @Override
    public String getMimeType(String filePath) {
        String mimeType = MimeTypeResolver.getMimeType(filePath);
        return ("application/octet-stream".equals(mimeType) ? null : mimeType);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        String actualPath = (path.endsWith("/") ? path : path + "/");
        Resource resource = this.resourceLoader.getResource(getResourceLocation(actualPath));
        try {
            File file = resource.getFile();
            String[] fileList = file.list();
            if (ObjectUtils.isEmpty(fileList)) {
                return null;
            }
            Set<String> resourcePaths = new LinkedHashSet<String>(fileList.length);
            for (String fileEntry : fileList) {
                String resultPath = actualPath + fileEntry;
                if (resource.createRelative(fileEntry).getFile().isDirectory()) {
                    resultPath += "/";
                }
                resourcePaths.add(resultPath);
            }
            return resourcePaths;
        } catch (IOException ex) {
            logger.warn("Couldn't get resource paths for " + resource, ex);
            return null;
        }
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
        if (!resource.exists()) {
            return null;
        }
        try {
            return resource.getURL();
        } catch (MalformedURLException ex) {
            throw ex;
        } catch (IOException ex) {
            logger.warn("Couldn't get URL for " + resource, ex);
            return null;
        }
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
        if (!resource.exists()) {
            return null;
        }
        try {
            return resource.getInputStream();
        } catch (IOException ex) {
            logger.warn("Couldn't open InputStream for " + resource, ex);
            return null;
        }
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("RequestDispatcher path at ServletContext level must start with '/'");
        }
        return new MockRequestDispatcher(path);
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String path) {
        return this.namedRequestDispatchers.get(path);
    }

    /**
     * Register a {@link RequestDispatcher} (typically a {@link MockRequestDispatcher})
     * that acts as a wrapper for the named Servlet.
     *
     * @param name              the name of the wrapped Servlet
     * @param requestDispatcher the dispatcher that wraps the named Servlet
     * @see #getNamedDispatcher
     * @see #unregisterNamedDispatcher
     */
    public void registerNamedDispatcher(String name, RequestDispatcher requestDispatcher) {
        Assert.notNull(name, "RequestDispatcher name must not be null");
        Assert.notNull(requestDispatcher, "RequestDispatcher must not be null");
        this.namedRequestDispatchers.put(name, requestDispatcher);
    }

    /**
     * 주어진 이름의 {@link RequestDispatcher}를 등록해제함.
     *
     * @param name 등록해제할 dispatcher 이름
     * @see #getNamedDispatcher
     * @see #registerNamedDispatcher
     */
    public void unregisterNamedDispatcher(String name) {
        Assert.notNull(name, "RequestDispatcher name must not be null");
        this.namedRequestDispatchers.remove(name);
    }

    /**
     * <em>default</em> {@code Servlet}의 이름 보기.
     * <p>Defaults to {@literal 'default'}.
     *
     * @see #setDefaultServletName
     */
    public String getDefaultServletName() {
        return this.defaultServletName;
    }

    /**
     * <em>default</em> {@code Servlet}의 이름 설정함.
     * 또한, 현재  {@link RequestDispatcher}를 {@link #unregisterNamedDispatcher unregisters}하고
     * 제공된 이름의 {@code defaultServletName}로 {@link MockRequestDispatcher}을 {@link #registerNamedDispatcher 등록함}
     *
     * @param defaultServletName <em>default</em> {@code Servlet} 이름;
     *                           절대 {@code null} 이나 빈값이 될 수 없음
     * @see #getDefaultServletName
     */
    public void setDefaultServletName(String defaultServletName) {
        Assert.hasText(defaultServletName, "defaultServletName must not be null or empty");
        unregisterNamedDispatcher(this.defaultServletName);
        this.defaultServletName = defaultServletName;
        registerNamedDispatcher(this.defaultServletName, new MockRequestDispatcher(this.defaultServletName));
    }

    @Override
    @Deprecated
    public Servlet getServlet(String name) {
        return null;
    }

    @Override
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration(new HashSet<Servlet>());
    }

    @Override
    @Deprecated
    public Enumeration<String> getServletNames() {
        return Collections.enumeration(new HashSet<String>());
    }

    @Override
    public void log(String message) {
        logger.info(message);
    }

    @Override
    @Deprecated
    public void log(Exception ex, String message) {
        logger.info(message, ex);
    }

    @Override
    public void log(String message, Throwable ex) {
        logger.info(message, ex);
    }

    @Override
    public String getRealPath(String path) {
        Resource resource = this.resourceLoader.getResource(getResourceLocation(path));
        try {
            return resource.getFile().getAbsolutePath();
        } catch (IOException ex) {
            logger.warn("Couldn't determine real path of resource " + resource, ex);
            return null;
        }
    }

    @Override
    public String getServerInfo() {
        return "MockServletContext";
    }

    @Override
    public String getInitParameter(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        return this.initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        Assert.notNull(name, "Parameter name must not be null");
        if (this.initParameters.containsKey(name)) {
            return false;
        }
        this.initParameters.put(name, value);
        return true;
    }

    public void addInitParameter(String name, String value) {
        Assert.notNull(name, "Parameter name must not be null");
        this.initParameters.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        Assert.notNull(name, "Attribute name must not be null");
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(new LinkedHashSet<String>(this.attributes.keySet()));
    }

    @Override
    public void setAttribute(String name, Object value) {
        Assert.notNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        Assert.notNull(name, "Attribute name must not be null");
        this.attributes.remove(name);
    }

    public void setServletContextName(String servletContextName) {
        this.servletContextName = servletContextName;
    }

    @Override
    public String getServletContextName() {
        return this.servletContextName;
    }

    @Override
    public ClassLoader getClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {
        Assert.notNull(roleNames, "Role names array must not be null");
        for (String roleName : roleNames) {
            Assert.hasLength(roleName, "Role name must not be empty");
            this.declaredRoles.add(roleName);
        }
    }

    public Set<String> getDeclaredRoles() {
        return Collections.unmodifiableSet(this.declaredRoles);
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
            throws IllegalStateException, IllegalArgumentException {
        this.sessionTrackingModes = sessionTrackingModes;
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return DEFAULT_SESSION_TRACKING_MODES;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return (this.sessionTrackingModes != null ?
                Collections.unmodifiableSet(this.sessionTrackingModes) : DEFAULT_SESSION_TRACKING_MODES);
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return this.sessionCookieConfig;
    }


    //---------------------------------------------------------------------
    // 지원하지 않는 Servlet 3.0 registration 메서드들
    //---------------------------------------------------------------------

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     * 이 메서드는 항상 {@code null}을 리턴함.
     *
     * @see javax.servlet.ServletContext#getServletRegistration(java.lang.String)
     */
    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    /**
     * 이 메서드는 항상 {@linkplain Collections#emptyMap empty map}을 리턴함.
     *
     * @see javax.servlet.ServletContext#getServletRegistrations()
     */
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Collections.emptyMap();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     * 이 메서드는 항상 {@code null}을 리턴함.
     *
     * @see javax.servlet.ServletContext#getFilterRegistration(java.lang.String)
     */
    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    /**
     * 이 메서드는 항상 {@linkplain Collections#emptyMap empty map}을 리턴함.
     *
     * @see javax.servlet.ServletContext#getFilterRegistrations()
     */
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return Collections.emptyMap();
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }


    /**
     * 내부 팩토리 클래스.
     * 실제로 MIME type을 해석요청 받을때, Java Activation Framework를 사용한다.
     */
    private static class MimeTypeResolver {

        public static String getMimeType(String filePath) {
            return FileTypeMap.getDefaultFileTypeMap().getContentType(filePath);
        }
    }

}
