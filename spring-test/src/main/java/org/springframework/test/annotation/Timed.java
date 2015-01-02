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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 테스트 실행이 {@link #millis() specified time period} 안에 끝나야 한다는 것을 명시하는
 * 테스트 전용 어노테이션.
 * <p/>
 * <p>
 * 만약 테스트 실행이 명시된 시간보다 길어지면, 테스트는 failed로 간주됨.
 * <p/>
 * <p>
 * 노트: 테스트 시간은 테스트 메서드 자신 뿐만 아니라, {@link Repeat repetitions}이나
 * <em>set up</em> 이나 <em>tear down</em>도 포함함.
 * </p>
 * <p/>
 * 스프링 프레임워크 4.0 이후, 이 어노테이션은 커스텀 <em>composed annotations</em> 어노테이션을 생성하기 위한
 * <em>meta-annotation</em>로 사용될 수 있음.
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @see Repeat
 * @since 2.0
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, ANNOTATION_TYPE})
public @interface Timed {

    /**
     * 테스트 실행이 너무 오래 걸려 실패로 기록 되기까지 실행 될 최대 시간(밀리세컨드).
     */
    long millis();

}
