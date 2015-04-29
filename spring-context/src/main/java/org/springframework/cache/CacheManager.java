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

package org.springframework.cache;

import java.util.Collection;

/**
 * 스프링의 중앙 캐시 매니저 SPI(Service Provider Interface).
 * {@link Cache} 리전들을 획득함.
 *
 * @author Costin Leau
 * @since 3.1
 */
public interface CacheManager {

	/**
	 * 주어진 이름의 캐시를 리턴함.
	 * @param name 캐시 이름({@code null}이면 안됨)
	 * @return 연관된 캐시, 없을시 {@code null} 리턴
	 */
	Cache getCache(String name);

	/**
	 * 이 cache manager에게 알려진 캐시 이름들을 리턴함.
	 * @return 캐시 이름들
	 */
	Collection<String> getCacheNames();

}
