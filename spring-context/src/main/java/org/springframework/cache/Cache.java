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

/**
 * 일반적인 캐시 operation을 정의한 인터페이스. <br />
 * <br />
 * <b>노트:</b>일반적인 캐싱의 사용을 위해, 스토리지가 <tt>null</tt>값을 허용하게 구현하기를 추천함. <br />
 * (예: {@code null}을 리턴하는 캐시 메소드)
 * 
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
public interface Cache {

	/**
	 * 캐시 이름을 리턴
	 */
	String getName();

	/**
	 * 네이티브 캐시 제공자를 리턴.
	 */
	Object getNativeCache();

	/**
	 * 파라미터 key값의 캐시 값을 리턴.
	 * 
	 * <p>  key값의 캐시가 없으면 {@code null}을 리턴함;
	 * 그 외의 경우, 캐시 값({@code null}값 일지라도)을 {@link ValueWrapper} 안에 포함하여 리턴함.
     * 
	 * @param key 캐시를 가져올 key값
	 * @return the value to which this cache maps the specified key,
	 * contained within a {@link ValueWrapper} which may also hold
	 * a cached {@code null} value. A straight {@code null} being
	 * returned means that the cache contains no mapping for this key.
	 * @see #get(Object, Class)
	 */
	ValueWrapper get(Object key);

	/**
	 * Return the value to which this cache maps the specified key,
	 * generically specifying a type that return value will be cast to.
	 * <p>Note: This variant of {@code get} does not allow for differentiating
	 * between a cached {@code null} value and no cache entry found at all.
	 * Use the standard {@link #get(Object)} variant for that purpose instead.
	 * @param key the key whose associated value is to be returned
	 * @param type the required type of the returned value (may be
	 * {@code null} to bypass a type check; in case of a {@code null}
	 * value found in the cache, the specified type is irrelevant)
	 * @return the value to which this cache maps the specified key
	 * (which may be {@code null} itself), or also {@code null} if
	 * the cache contains no mapping for this key
	 * @throws IllegalStateException if a cache entry has been found
	 * but failed to match the specified type
	 * @see #get(Object)
	 * @since 4.0
	 */
	<T> T get(Object key, Class<T> type);

	/**
	 * Associate the specified value with the specified key in this cache.
	 * <p>If the cache previously contained a mapping for this key, the old
	 * value is replaced by the specified value.
	 * @param key the key with which the specified value is to be associated
	 * @param value the value to be associated with the specified key
	 */
	void put(Object key, Object value);

	/**
	 * Atomically associate the specified value with the specified key in this cache
	 * if it is not set already.
	 * <p>This is equivalent to:
	 * <pre><code>
	 * Object existingValue = cache.get(key);
	 * if (existingValue == null) {
	 *     cache.put(key, value);
	 *     return null;
	 * } else {
	 *     return existingValue;
	 * }
	 * </code></pre>
	 * except that the action is performed atomically. While all out-of-the-box
	 * {@link CacheManager} implementations are able to perform the put atomically,
	 * the operation may also be implemented in two steps, e.g. with a check for
	 * presence and a subsequent put, in a non-atomic way. Check the documentation
	 * of the native cache implementation that you are using for more details.
	 * @param key the key with which the specified value is to be associated
	 * @param value the value to be associated with the specified key
	 * @return the value to which this cache maps the specified key (which may be
	 * {@code null} itself), or also {@code null} if the cache did not contain any
	 * mapping for that key prior to this call. Returning {@code null} is therefore
	 * an indicator that the given {@code value} has been associated with the key.
	 * @since 4.1
	 */
	ValueWrapper putIfAbsent(Object key, Object value);

	/**
	 * 만약 key값의 캐시가 존재하면 제거함.
	 * @param key 제거할 캐시 key값
	 */
	void evict(Object key);

	/**
	 * 캐시의 모든 매핑을 제거함.
	 * 
	 */
	void clear();


	/**
	 * 캐시 값을 포함한 wrapping 객체.
	 */
	interface ValueWrapper {

		/**
		 * 실제 캐시 값을 리턴
		 */
		Object get();
	}

}
