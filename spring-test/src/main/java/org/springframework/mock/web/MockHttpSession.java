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

import org.springframework.util.Assert;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;
import java.util.*;

/**
 * {@link javax.servlet.http.HttpSession} 인터페이스의 mock 구현.
 * <p>
 * Spring Framework 4.0에서, 이 클래스는 Servlet 3.0으로 디자인됨.
 * <p>
 * <p>web framework테스트에 사용함; 또한 어플리케이션 컨트롤러 테스트에 유용함.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Mark Fisher
 * @author Sam Brannen
 * @since 1.0.2
 */
@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {

    public static final String SESSION_COOKIE_NAME = "JSESSION";


    private static int nextId = 1;

    private String id;

    private final long creationTime = System.currentTimeMillis();

    private int maxInactiveInterval;

    private long lastAccessedTime = System.currentTimeMillis();

    private final ServletContext servletContext;

    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    private boolean invalid = false;

    private boolean isNew = true;


    /**
     * 기본 {@link MockServletContext}로 MockHttpSession을 생성함.
     *
     * @see MockServletContext
     */
    public MockHttpSession() {
        this(null);
    }

    /**
     * 주어진 ServeltContext로 MockHttpSession을 생성함.
     *
     * @param servletContext 세션이 실행될 ServletContext
     */
    public MockHttpSession(ServletContext servletContext) {
        this(servletContext, null);
    }

    /**
     * 주어진 ServeltContext와 id로 MockHttpSession을 생성함.
     *
     * @param servletContext 세션이 실행될 ServletContext
     * @param id             세션을 위한 유니크 id
     */
    public MockHttpSession(ServletContext servletContext, String id) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.id = (id != null ? id : Integer.toString(nextId++));
    }

    @Override
    public long getCreationTime() {
        assertIsValid();
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Servlet 3.1에서 세션 id는 바뀔수 있음.
     *
     * @return 새 세션 id
     * @since 4.0.3
     */
    public String changeSessionId() {
        this.id = Integer.toString(nextId++);
        return this.id;
    }

    public void access() {
        this.lastAccessedTime = System.currentTimeMillis();
        this.isNew = false;
    }

    @Override
    public long getLastAccessedTime() {
        assertIsValid();
        return this.lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("getSessionContext");
    }

    @Override
    public Object getAttribute(String name) {
        assertIsValid();
        Assert.notNull(name, "Attribute name must not be null");
        return this.attributes.get(name);
    }

    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        assertIsValid();
        return Collections.enumeration(new LinkedHashSet<String>(this.attributes.keySet()));
    }

    @Override
    public String[] getValueNames() {
        assertIsValid();
        return this.attributes.keySet().toArray(new String[this.attributes.size()]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        assertIsValid();
        Assert.notNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
            if (value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
            }
        } else {
            removeAttribute(name);
        }
    }

    @Override
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        assertIsValid();
        Assert.notNull(name, "Attribute name must not be null");
        Object value = this.attributes.remove(name);
        if (value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
        }
    }

    @Override
    public void removeValue(String name) {
        removeAttribute(name);
    }

    /**
     * 모든 세션 attribute를 clear.
     */
    public void clearAttributes() {
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
            }
        }
    }

    /**
     * 이 세션을 무효화 하고 모든 object 바인딩을 초기화함.
     *
     * @throws IllegalStateException 이미 세션이 무효화 된 상태에서 호출하면 발생
     */
    @Override
    public void invalidate() {
        assertIsValid();
        this.invalid = true;
        clearAttributes();
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    /**
     * 이 세션이 {@linkplain #invalidate() invalidated}를 호출하지 않은것을 확인하는데 편리한 메서드.
     *
     * @throws IllegalStateException 이미 세션이 무효화 된 상태에서 호출하면 발생
     */
    private void assertIsValid() {
        if (isInvalid()) {
            throw new IllegalStateException("The session has already been invalidated");
        }
    }

    public void setNew(boolean value) {
        this.isNew = value;
    }

    @Override
    public boolean isNew() {
        assertIsValid();
        return this.isNew;
    }

    /**
     * 이 세션의 attribute들을 직렬화 하여 object로 만듦, 이 byte array는 표준 Java 직렬화로 만들어짐.
     *
     * @return 세션의 직렬화 된 상태를 리턴
     */
    public Serializable serializeState() {
        HashMap<String, Serializable> state = new HashMap<String, Serializable>();
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof Serializable) {
                state.put(name, (Serializable) value);
            } else {
                // 직렬화 불가능... Servlet 컨테이너들은 보통 이런 케이스에 자동으로 attribute들의 바인딩을 초기화함.
                if (value instanceof HttpSessionBindingListener) {
                    ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
                }
            }
        }
        return state;
    }

    /**
     * {@link #serializeState()}로 만든 state object로 부터 역직렬화 해서 세션 상태를 가져옴.
     *
     * @param state 이 세션의 직렬화된 상태를 담은 object
     */
    @SuppressWarnings("unchecked")
    public void deserializeState(Serializable state) {
        Assert.isTrue(state instanceof Map, "Serialized state needs to be of type [java.util.Map]");
        this.attributes.putAll((Map<String, Object>) state);
    }

}
