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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * 테스트 메서드가 완료된 후 트랜젝션이 <em>rolled back</em>되어야 하는지 명시하는 어노테이션.
 * 만약 {@code true}값이면, 트랜젝션이 롤백;반대의 경우, 트랜젝션이 커밋됨.
 *
 * 스프링 프레임워크 4.0 이후, 이 어노테이션은 커스텀 <em>composed annotations</em> 어노테이션을 생성하기 위한
 * <em>meta-annotation</em>로 사용될 수 있음.
 *
 * @author Sam Brannen
 * @since 2.5
 */
@Documented
@Retention(RUNTIME)
@Target({ METHOD, ANNOTATION_TYPE })
public @interface Rollback {

	/**
     * 어노테이션이 적용된 메서드가 실행 완료 후 트랜젝션이 롤백될지 여부.
	 */
	boolean value() default true;

}
