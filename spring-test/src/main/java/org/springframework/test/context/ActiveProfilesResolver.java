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

package org.springframework.test.context;

/**
 * 전략 인터페이스, 어떤 <em>active bean definition profiles</em>이 {@link org.springframework.context.ApplicationContext ApplicationContext}
 * 로딩 때 사용되어야 하는지 코드로 정의.
 * <p>
 * <p>커스턴 {@code ActiveProfilesResolver}은 {@code @ActiveProfiles}의 {@link ActiveProfiles#resolver resolver} 통해서 등록 가능함.</p>
 * <p>
 * <p>구체적인 구현체는 반드시 인자가 없는 {@code public} 생성자를 제공해야함.</p>
 *
 * @author Sam Brannen
 * @author Michail Nikolaev
 * @see ActiveProfiles
 * @since 4.0
 */
public interface ActiveProfilesResolver {

    /**
     *
     * 어떤 <em>active bean definition profiles</em>이 {@link org.springframework.context.ApplicationContext ApplicationContext}
     * 로딩 때 사용되어야 하는지 정의.
     *
     * @param testClass 어떤 profiles가 reslove되어야 하는지 정의한 테스트 클래스;
     *                  절대 {@code null}이면 안됨
     * @return {@code ApplicationContext}때 사용할 bean definition profiles 리스트;절대 {@code null}이면 안됨
     * @see ActiveProfiles#resolver
     * @see ActiveProfiles#inheritProfiles
     */
    String[] resolve(Class<?> testClass);

}
