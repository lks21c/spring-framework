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

package org.springframework.mock.jndi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import java.util.Hashtable;

/**
 * 간단한 JNDI naming context builder의 구현.
 * <p>
 * <p> 주된 타겟은 테스트 environment로,
 * 각 테스트 케이스가 JNDI를 올바르게 설정하여 {@code new InitialContext()}가 필요한 Object들을 노출할수 있게함.
 * 또한, standalone 어플리케이션들에도 유용함.
 * (예: JDBC DataSource를 잘 알려진 JNDI 위치로 바인딩 할때로, 전통적인 2EE data 엑세스 코드를 J2EE 컨테이너 바깥에서 사용하게 할때)
 * <p>
 * <p>
 * <p>다양한 Datasource 구현 옵션이 있음:
 * <ul>
 * <li>{@code SingleConnectionDataSource} (모든 getConnection 호출에서 같은 커넥션을 쓸때)
 * <li>{@code DriverManagerDataSource} (각 getConnection 호출에서 새 커넥션을 생성할때)
 * <li>Apache's Jakarta Commons DBCP offers {@code org.apache.commons.dbcp.BasicDataSource} (실제 pool)
 * </ul>
 * <p>
 * <p>일반적인 사용법 예제:
 * <p>
 * <pre class="code">
 * SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
 * DataSource ds = new DriverManagerDataSource(...);
 * builder.bind("java:comp/env/jdbc/myds", ds);
 * builder.activate();</pre>
 * <p>
 * <p>
 * 노트 : multiple builder를 같은 JVM에서 활성화 하는 것은 불가능함.
 * 그러므로, 반복적으로 builder를 설정하기 위해서, 아래의 코드를 사용해서 이미 활성화된 builder를 가져오거나
 * 새롭게 활성화 해야함:
 * </p>
 * <p>
 * <pre class="code">
 * SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
 * DataSource ds = new DriverManagerDataSource(...);
 * builder.bind("java:comp/env/jdbc/myds", ds);</pre>
 * <p>
 * <p>
 * 노트 : 이미 활성화된 context가 있을수 있음에 따라 사용자는 builder에서 <i>{@code activate()} 메서드를 호출하면 안됨</i>.
 * </p>
 * <p>
 * <p>
 * 이 클래스의 인스턴스는 오직 setup 시간에만 필요함.
 * 어플리케이션은 활성화 이후에는 인스턴스의 reference를 유지할 필요가 없음.
 * </p>
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @see #emptyActivatedContextBuilder()
 * @see #bind(String, Object)
 * @see #activate()
 * @see SimpleNamingContext
 * @see org.springframework.jdbc.datasource.SingleConnectionDataSource
 * @see org.springframework.jdbc.datasource.DriverManagerDataSource
 */
public class SimpleNamingContextBuilder implements InitialContextFactoryBuilder {

    /**
     * JNDI에 바인딩 될 이 클래스이 인스턴스
     */
    private static volatile SimpleNamingContextBuilder activated;

    private static boolean initialized = false;

    private static final Object initializationLock = new Object();


    /**
     * SimpleNamingContextBuilder가 활성되었는지 체크함.
     *
     * @return 현재 SimpleNamingContextBuilder 인스턴스나 인스턴스가 없으면 {@code null}
     */
    public static SimpleNamingContextBuilder getCurrentContextBuilder() {
        return activated;
    }

    /**
     * 아직 JNDI에 설정된 SimpleNamingContextBuilder가 없으면 생성해서 활성화 함.
     * 그게 아니면, 기존에 활성화 된 SimpleNamingContextBuilder를 clear해서 리턴함.
     * <p>
     * <p>
     * 이 메서드의 주목적은 반복적으로 JNDI 바인딩을 재초기화 하고 싶은 테스트 suite를 위해 존재함.
     *
     * @return JNDI 바인딩을 컨트롤 하기 위해 사용할 빈 SimpleNamingContextBuilder
     * </p>
     */
    public static SimpleNamingContextBuilder emptyActivatedContextBuilder() throws NamingException {
        if (activated != null) {
            // 이미 활성화된 context builder를 clear함.
            activated.clear();
        } else {
            // 새 context builder를 생성하고 활성화 함.
            SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
            // activate() 호출이 activated 변수에 값을 넣음.
            builder.activate();
        }
        return activated;
    }


    private final Log logger = LogFactory.getLog(getClass());

    private final Hashtable<String, Object> boundObjects = new Hashtable<String, Object>();


    /**
     * context builder를 JNDI NamingManager에 등록함. <br /><br />
     * <p>
     * 노트 : 이 메서드가 완료되면, {@code new InitialContext()}가 항상 이 팩토리로부터 컨텍스트를 리턴함.<br />
     * 빈 컨텍스트를 얻으려면 {@code emptyActivatedContextBuilder()를 호출해야함. (예: 테스트 메서드에서).<br />
     *
     * @throws IllegalStateException 이미 JNDI NamingManager에 등록된 context builder가 있을 때 발생
     */
    public void activate() throws IllegalStateException, NamingException {
        logger.info("Activating simple JNDI environment");
        synchronized (initializationLock) {
            if (!initialized) {
                if (NamingManager.hasInitialContextFactoryBuilder()) {
                    throw new IllegalStateException(
                            "Cannot activate SimpleNamingContextBuilder: there is already a JNDI provider registered. " +
                                    "Note that JNDI is a JVM-wide service, shared at the JVM system class loader level, " +
                                    "with no reset option. As a consequence, a JNDI provider must only be registered once per JVM.");
                }
                NamingManager.setInitialContextFactoryBuilder(this);
                initialized = true;
            }
        }
        activated = this;
    }

    /**
     * 임시로 context builder를 비활성화 함.
     * JNDI NamingManager에 등록된 상태는 유지하지만, (만약 설정되었다면) 표준 JNDI InitialContextFactory로 전달함.
     * <p>
     * <p>
     * context builder의 바인딩 된 objects를 노출하려면, {@code activate()}를 다시 호출하면 됨.
     * activate/deactivate는 몇번이든 적용됨(예: 같은 JVM에서 도는 대규모 통합 테스트 suite)
     * </p>
     */
    public void deactivate() {
        logger.info("Deactivating simple JNDI environment");
        activated = null;
    }

    /**
     * 이 context builder의 모든 바인딩을 clear함. 활성화 상태는 유지함.
     */
    public void clear() {
        this.boundObjects.clear();
    }

    /**
     * 주어진 이름으로 주어진 object를 바인딩함, 이 context builder가 생성할 모든 naiming context들에 적용.
     *
     * @param name 이 Object의 JNDI 이름 (예: "java:comp/env/jdbc/myds")
     * @param obj  바인딩 될 object (예: a DataSource implementation)
     */
    public void bind(String name, Object obj) {
        if (logger.isInfoEnabled()) {
            logger.info("Static JNDI binding: [" + name + "] = [" + obj + "]");
        }
        this.boundObjects.put(name, obj);
    }


    /**
     * 간단한 InitialContextFactoryBuilder 의 구현으로,
     * 새 SimpleNamingContext를 생성함.
     *
     * @see SimpleNamingContext
     */
    @Override
    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) {
        if (activated == null && environment != null) {
            Object icf = environment.get(Context.INITIAL_CONTEXT_FACTORY);
            if (icf != null) {
                Class<?> icfClass;
                if (icf instanceof Class) {
                    icfClass = (Class<?>) icf;
                } else if (icf instanceof String) {
                    icfClass = ClassUtils.resolveClassName((String) icf, getClass().getClassLoader());
                } else {
                    throw new IllegalArgumentException("Invalid value type for environment key [" +
                            Context.INITIAL_CONTEXT_FACTORY + "]: " + icf.getClass().getName());
                }
                if (!InitialContextFactory.class.isAssignableFrom(icfClass)) {
                    throw new IllegalArgumentException(
                            "Specified class does not implement [" + InitialContextFactory.class.getName() + "]: " + icf);
                }
                try {
                    return (InitialContextFactory) icfClass.newInstance();
                } catch (Throwable ex) {
                    throw new IllegalStateException("Cannot instantiate specified InitialContextFactory: " + icf, ex);
                }
            }
        }

        // 기본 케이스...
        return new InitialContextFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public Context getInitialContext(Hashtable<?, ?> environment) {
                return new SimpleNamingContext("", boundObjects, (Hashtable<String, Object>) environment);
            }
        };
    }

}
