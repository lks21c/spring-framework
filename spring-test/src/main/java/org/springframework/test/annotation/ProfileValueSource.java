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

package org.springframework.test.annotation;

/**
 * <p>
 * 주어진 테스트 환경의 <em>profile values</em>을 얻기 위한 전략 인터페이스.
 * </p>
 * <p>
 * <p>
 * 실제 구현체는 반드시 {@code public}의 인자값 없는 생성자를 제공해야함.
 * </p>
 * <p>
 * <p>
 * <p>
 * 스프링은 아래의 컨테이너와 관계없는 구현체를 제공함:
 * </p>
 * <ul>
 * <li>{@link SystemProfileValueSource}</li>
 * </ul>
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @see ProfileValueSourceConfiguration
 * @see IfProfileValue
 * @see ProfileValueUtils
 * @since 2.0
 */
public interface ProfileValueSource {

    /**
     * key에 명시된 <em>profile value</em>를 가져옴.
     *
     * @param key <em>profile value</em> 이름
     * @return <em>profile value</em> 문자열이나 <em>profile value</em>이 없으면 {@code null}
     */
    String get(String key);

}
