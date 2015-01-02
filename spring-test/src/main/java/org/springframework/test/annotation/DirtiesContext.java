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

package org.springframework.test.annotation;

import java.lang.annotation.*;

/**
 * 테스트와 연관된 {@link org.springframework.context.ApplicationContext ApplicationContext}가
 * <em>dirty</em>이고 반드시 종료되어야 한다는것을 알려주는 어노테이션:
 * <p>
 * <ul>
 * <li>현재 테스트 이후, 메서드 레벨에서 정의되었을 때</li>
 * <li>현재 테스트 클래스의 각 메서드 이후, 클래스 레벨에 클래스 모드와 설정시 {@link ClassMode#AFTER_EACH_TEST_METHOD
 * AFTER_EACH_TEST_METHOD}</li>
 * <li>현재 테스트 클래스 이후, 클레스 레벨에서 클래스 모드로 설정 시 {@link ClassMode#AFTER_CLASS AFTER_CLASS}</li>
 * </ul>
 * <p>
 * 만약 테스트가 context 변경 시 이 어노테이션을 사용할 것; 예를 들어,
 * bean definition을 치환하거나 싱글턴 bean 상태를 변경했을 때.
 * 그다음 테스트들은 새 context를 제공받음.
 * <p>
 * <p>
 * {@code @DirtiesContext}는 클래스 level과 메서드 level에서 어노테이션을 사용 가능함.
 * 이 시나리오에서, {@code ApplicationContext}는 어떤 메서드나 클래스 실행 되에 <em>dirty</em>로 마크됨.
 * 만약 {@link ClassMode}가 {@link ClassMode#AFTER_EACH_TEST_METHOD
 * AFTER_EACH_TEST_METHOD}로 설정되면 context는 각 테스트 메서드 수행 이후 dirty로 마크됨.
 * <p>
 * <p>
 * <p>
 * <p>
 * 스프링 프레임워크 4.0 이후, 이 어노테이션은 커스텀 <em>composed annotations</em> 어노테이션을 생성하기 위한
 * <em>meta-annotation</em>로 사용될 수 있음.
 * </p>
 * <p>
 *
 * @author Sam Brannen
 * @author Rod Johnson
 * @see org.springframework.test.context.ContextConfiguration
 * @since 2.0
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DirtiesContext {

    /**
     * 클래스 <i>modes</i>를 정의.
     * 테스트 클래스에 어노테이션이 붙을 때 {@code @DirtiesContext}가 어떻게 해석될지 정의.
     *
     * @since 3.0
     */
    static enum ClassMode {

        /**
         * 연관된 {@code ApplicationContext}가 테스트 클래스 이후 <em>dirty</em>로 마크됨.
         */
        AFTER_CLASS,

        /**
         * 연관된 {@code ApplicationContext}가 테스트 메서드 이후 <em>dirty</em>로 마크됨.
         */
        AFTER_EACH_TEST_METHOD;
    }

    /**
     * <i>modes</i>를 정의.
     * {@link org.springframework.test.context.ContextHierarchy @ContextHierarchy}의 부분으로
     * {@code @DirtiesContext}가 test에서 쓰였을 때 context cache가 어떻게 clear되는지 정의함.
     *
     * @since 3.2.2
     */
    static enum HierarchyMode {

        /**
         * <em>exhaustive</em> 알고리즘을 이용하여 context cache를 clear함.
         * {@linkplain HierarchyMode#CURRENT_LEVEL current level} 뿐만 아니라 조상 context를 공유하는 현재 테스트의 모든 다른 context 구조들을 포함함.
         * <p>
         * 공통 조상 context 의 하부 구조에 위치한 모든 {@code ApplicationContexts}는 context cache에서 clear되고 종료됨.
         * </p>
         */
        EXHAUSTIVE,

        /**
         * <em>current level</em>로 context cache를 clear 함.
         * context 구조 안의  {@code ApplicationContext}와 모든 current lovel의 하부구조의 context의
         * context cache가 clear되고 종료됨.
         * <p>
         * current lovel은 context 구조의 가장 낮은 레벨의 {@code ApplicationContext}를 뜻함.
         * (현재 테스트에서 visible한 context들)
         */
        CURRENT_LEVEL;
    }


    /**
     * 이 <i>mode</i>는 {@code @DirtiesContext}을 사욜할 때 쓴다.
     * 기본값은 {@link ClassMode#AFTER_CLASS AFTER_CLASS}.
     * <p>
     * 노트: 메소드에 해당 어노테이션을 적용하면 의미 없음, 왜냐하면 메소드에 {@code @DirtiesContext}을 적용하는것
     * 만으로 충분하기 때문임.
     * </p>
     *
     * @since 3.0
     */
    ClassMode classMode() default ClassMode.AFTER_CLASS;

    /**
     * context cache clear <em>mode</em>.
     * context가 {@link org.springframework.test.context.ContextHierarchy @ContextHierarchy}를 통해
     * 구조적 context의 일부로 설정시 사용함.
     * 기본값은 {@link HierarchyMode#EXHAUSTIVE EXHAUSTIVE}.
     *
     *
     * @since 3.2.2
     */
    HierarchyMode hierarchyMode() default HierarchyMode.EXHAUSTIVE;

}
