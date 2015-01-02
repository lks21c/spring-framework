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
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link IfProfileValue&#064;IfProfileValue} 어노테이션으로 설정된 프로파일 값을 얻을 때
 * 어떤 타입의 {@link ProfileValueSource}를 사용할 지를 지정하는 클래스 레벨의 어노테이션임.
 * 스프링 프레임워크 4.0 이후, 이 어노테이션은 커스텀 <em>composed annotations</em> 어노테이션을 생성하기 위한
 * <em>meta-annotation</em>로 사용될 수 있음.
 *
 * @author Sam Brannen
 * @since 2.5
 * @see ProfileValueSource
 * @see IfProfileValue
 * @see ProfileValueUtils
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProfileValueSourceConfiguration {

	/**
	 * <p>
	 *   <em>profile values</em>를 얻을때 사용할 {@link ProfileValueSource} 타입
	 * </p>
	 *
	 * @see SystemProfileValueSource
	 */
	Class<? extends ProfileValueSource> value() default SystemProfileValueSource.class;

}
