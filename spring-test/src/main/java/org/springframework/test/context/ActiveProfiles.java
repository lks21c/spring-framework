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

package org.springframework.test.context;

import java.lang.annotation.*;

/**
 * {@code ActiveProfiles}은 클래스 레벨 어노테이션으로써 어떤 <em>active bean definition profiles</em>이
 * 테스트 클래스에서 {@link org.springframework.context.ApplicationContext ApplicationContext}을 로딩할때
 * 사용되어야 할 지 정의.
 * <p>
 * 스프링 프레임워크 4.0 이후, 이 어노테이션은 커스텀 <em>composed annotations</em> 어노테이션을 생성하기 위한
 * <em>meta-annotation</em>로 사용될 수 있음.
 *
 * @author Sam Brannen
 * @see SmartContextLoader
 * @see MergedContextConfiguration
 * @see ContextConfiguration
 * @see ActiveProfilesResolver
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.annotation.Profile
 * @since 3.1
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ActiveProfiles {

    /**
     * {@link #profiles}의 alias.
     * <p>
     * <p>
     * 이 어트리뷰트는 {@link #profiles}과 같이 사용하지 <strong>못 할 수</strong> 있으나,
     * {@link #profiles} <strong>대신</strong> 사용 할 수 있음.
     */
    String[] value() default {};

    /**
     * 활성화 시킬 빈 설정 프로파일.
     */
    String[] profiles() default {};

    /**
     * active bean definition profiles을 코드로 resloing 하는데 사용 할 {@link ActiveProfilesResolver} 타입.
     *
     * @see ActiveProfilesResolver
     * @since 4.0
     */
    Class<? extends ActiveProfilesResolver> resolver() default ActiveProfilesResolver.class;

    /**
     * 부모 클래스의 bean definition profiles을 <em>상속 받을 지</em> 여부.
     * <p>
     * 기본 값은 {@code true}, 즉 테스트 클래스는 부모 클래스의 bean definition profiles를 <em>상속받음</em>.
     * 구체적으로, 자식 테스트 클래스의 bean definition profiles은 부모 테스트 클래스의 bean definition profiles 리스트에
     * 추가 됨.
     * </p>
     * <p>
     * 만약 {@code inheritProfiles}이 {@code false}로 설정되면, 테스트 클래스를 위한 부모 클래스의
     * bean definition profiles은 <em>가려짐</em>.
     * <p>
     * <p>
     * 아래의 예제에서, {@code BaseTest}를 위한 {@code ApplicationContext}은 &quot;base&quot;
     * bean definition profile에 한해 로딩함;그러므로 &quot;extended&quot; profile은 로딩되지 않음.
     * 반대로, {@code ExtendedTest}는 &quot;extended&quot; profile만 로딩함.
     * <pre class="code">
     * &#064;ActiveProfiles(&quot;base&quot;)
     * &#064;ContextConfiguration
     * public class BaseTest {
     * // ...
     * }
     *
     * &#064;ActiveProfiles(&quot;extended&quot;)
     * &#064;ContextConfiguration
     * public class ExtendedTest extends BaseTest {
     * // ...
     * }
     * </pre>
     * <p>
     * <p>
     * 노트: {@code @ActiveProfiles}은 path-based resource 나 어노테이션으로 {@code ApplicationContext}를 로딩 할 수 있음.
     * </p>
     *
     * @see ContextConfiguration#locations
     * @see ContextConfiguration#classes
     * @see ContextConfiguration#inheritLocations
     */
    boolean inheritProfiles() default true;

}
