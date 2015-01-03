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
 * {@code @BootstrapWith}는 클레스 레벨 메타 데이터를 정의함, 어떻게 <em>Spring TestContext Framework</em>를 로딩하는지 정의하는데 쓰여짐.
 *
 * 이 어노테이션은 커스텀 <em>composed annotations</em> 어노테이션을 생성하기 위한
 * <em>meta-annotation</em>로 사용될 수 있음.
 *
 * @author Sam Brannen
 * @since 4.1
 * @see BootstrapContext
 * @see TestContextBootstrapper
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BootstrapWith {

	/**
	 * {@link TestContextBootstrapper}가 <em>Spring
	 * TestContext Framework</em>를 로딩하는데 쓰여짐.
	 */
	Class<? extends TestContextBootstrapper> value() default TestContextBootstrapper.class;

}
