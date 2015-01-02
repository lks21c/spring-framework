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

package org.springframework.test.annotation;

import java.lang.annotation.*;

/**
 * <p>
 * 테스트 어노테이션 으로 특정 테스트 profile이나 environment를 가리킨다.
 * 만약 설정된 {@link ProfileValueSource}가 제공된 이름으로 매치되는 {@link #value}를 리턴하면,
 * 테스트는 활성화됨.
 * </p>
 * <p>
 * <p>
 * 노트: {@code @IfProfileValue}는 class level이나 method level 혹은 둘다 적용될수 있음.
 * {@code @IfProfileValue}이 class level에 적용되면, 어떤 메소드던 {@code @IfProfileValue}가 되어 있더라도
 * class level에 override함.
 *
 * </p>
 * <p>
 * <h3>예제</h3>
 *
 * <p>
 * {@link SystemProfileValueSource}를 {@link ProfileValueSource}으로 사용할 때,
 * 아래의 방법으로 Sun Microsystems의 JVM에서만 테스트가 실행되게 할수 있음:
 * </p>
 * <pre class="code">
 * &#064;IfProfileValue(name = &quot;java.vendor&quot;, value = &quot;Sun Microsystems Inc.&quot;)
 * public void testSomething() {
 * // ...
 * }
 * </pre>
 * <p>
 *
 * </p>
 * {@code @IfProfileValue}을 아래와 같이 복수의 {@link #values() values}값으로 지정하면 <em>OR</em>의 의미임. <br />
 * ({@link ProfileValueSource}가 적절하게 &quot;test-groups&quot;가 name으로 설정되었다고 가정):
 * </p>
 * <p>
 * <pre class="code">
 * &#064;IfProfileValue(name = &quot;test-groups&quot;, values = { &quot;unit-tests&quot;, &quot;integration-tests&quot; })
 * public void testWhichRunsForUnitOrIntegrationTestGroups() {
 * // ...
 * }
 * </pre>
 * <p>
 * <p>
 * 스프링 프레임워크 4.0 이후, 이 어노테이션은 커스텀 <em>composed annotations</em> 어노테이션을 생성하기 위한
 * <em>meta-annotation</em>로 사용될 수 있음.
 * </p>
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @see ProfileValueSource
 * @see ProfileValueSourceConfiguration
 * @see ProfileValueUtils
 * @see org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @since 2.0
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface IfProfileValue {

    /**
     * 설정되었는지 확인할 {@code name}
     */
    String name();

    /**
     * 주어진 {@link #name() name}의 단일의 허용되는 <em>profile value</em>의 {@code value}.
     * <p>
     * 노트: {@link #value()} and {@link #values()} 양쪽에 값을 설정하면 conflict가 발생함.
     * </p>
     */
    String value() default "";

    /**
     * 주어진 {@link #name() name}의 리스트의 모든 허용되는 <em>profile value</em>의 {@code value}.
     * A list of all permissible {@code values} of the
     * <p>
     * 노트: {@link #value()} and {@link #values()} 양쪽에 값을 설정하면 conflict가 발생함.
     * </p>
     */
    String[] values() default {};

}
