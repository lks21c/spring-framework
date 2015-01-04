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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.style.ToStringCreator;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 테스트 환경의 스프링 {@link ApplicationContext ApplicationContexts}를 위한 캐시.
 * {@link MergedContextConfiguration} 인스턴스 들의 {@code ApplicationContexts} 캐시를 유지함.
 *
 *
 * <p>이것은 컨텍스트 초기화가 시간이 걸릴 시 중요한 퍼포먼스 이점이 있음.
 * spring context 자신은 매우 빠르게 초기화 되는동안에도, 컨텍스트의 몇몇 빈들(예를 들어
 * 하이버 네이트와 연동하는 {@code LocalSessionFactoryBean})은 초기화에 시간이 꽤 거릴수 있음.
 * 그러므로 test suite에서 한번의 초기화만 수행하는 것이 말이 됨.
 * </p>
 *
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 2.5
 */
class ContextCache {

	/**
	 * Spring {@code ApplicationContext} 인스턴스의 key Map.
	 */
	private final Map<MergedContextConfiguration, ApplicationContext> contextMap =
			new ConcurrentHashMap<MergedContextConfiguration, ApplicationContext>(64);

	/**
	 * 자식 key들을 가진 부모 key들의 Map으로, context 구조의 top-down <em>트리</em>를 표현.
	 * 이 정보는 다른 컨텍스트들의 부모 컨텍스트를 지울 때 어떤 자식 트리가 재귀적으로 지워지고 닫혀야 할지 정의함.
	 */
	private final Map<MergedContextConfiguration, Set<MergedContextConfiguration>> hierarchyMap =
			new ConcurrentHashMap<MergedContextConfiguration, Set<MergedContextConfiguration>>(64);

	private final AtomicInteger hitCount = new AtomicInteger();

	private final AtomicInteger missCount = new AtomicInteger();


	/**
	 * 캐시로 부터 모든 컨텍스트와 context 구조 정보를 를 clear함.
	 */
	public void clear() {
		this.contextMap.clear();
		this.hierarchyMap.clear();
	}

	/**
	 * 캐시의 hit/miss 카운트를 clear함(i.e., 0으로 카운터 리셋).
	 */
	public void clearStatistics() {
		this.hitCount.set(0);
		this.missCount.set(0);
	}

	/**
	 *
	 * 주어진 키로 캐시된 컨텍스트가 있는지 확인.
	 * @param key context key (절대 {@code null}이면 안됨)
	 * @return {@code true} 만약 캐시가 주어진 key로 컨텍스트를 포함하고 있는 경우
	 */
	public boolean contains(MergedContextConfiguration key) {
		Assert.notNull(key, "Key must not be null");
		return this.contextMap.containsKey(key);
	}

	/**
	 * 주어진 key로 캐시된 {@code ApplicationContext}을 획득함.
	 * <p>이에 따라 {@link #getHitCount() hit}와 {@link #getMissCount() miss} 카운트가 업데이트 됨.</p>
	 * @param key context key (절대 {@code null}이면 안됨)
	 * @return 원하는 {@code ApplicationContext} 인스턴스, 없을 시 {@code null}
	 * @see #remove
	 */
	public ApplicationContext get(MergedContextConfiguration key) {
		Assert.notNull(key, "Key must not be null");
		ApplicationContext context = this.contextMap.get(key);
		if (context == null) {
			this.missCount.incrementAndGet();
		}
		else {
			this.hitCount.incrementAndGet();
		}
		return context;
	}

	/**
	 * 이 캐시를 위한 종합적인 히트 카운트를 리턴.
	 * <p><em>hit</em> 는 캐시에 접근시도 시, null이 아닌 컨텍스트를 요청한 key로부터 리턴하는 것.</p>
	 */
	public int getHitCount() {
		return this.hitCount.get();
	}

	/**
	 * 이 캐시를 위한 종합적인 미스 카운트를 리턴.
	 * <p></p><em>miss</em>는 캐시에 접근시도 시, {@code null}을 요청한 key로부터 리턴하는 것.</p>
	 */
	public int getMissCount() {
		return this.missCount.get();
	}

	/**
	 * 주어진 키의 {@code ApplicationContext} 인스턴스를 명시적으로 캐시에 추가함.
	 * @param key context key (절대 {@code null}이면 안됨)
	 * @param context {@code ApplicationContext} 인스턴스 (절대 {@code null}이면 안됨)
	 */
	public void put(MergedContextConfiguration key, ApplicationContext context) {
		Assert.notNull(key, "Key must not be null");
		Assert.notNull(context, "ApplicationContext must not be null");

		this.contextMap.put(key, context);
		MergedContextConfiguration child = key;
		MergedContextConfiguration parent = child.getParent();
		while (parent != null) {
			Set<MergedContextConfiguration> list = this.hierarchyMap.get(parent);
			if (list == null) {
				list = new HashSet<MergedContextConfiguration>();
				this.hierarchyMap.put(parent, list);
			}
			list.add(child);
			child = parent;
			parent = child.getParent();
		}
	}

	/**
	 * {@link ConfigurableApplicationContext}의 인스턴스 라면,
	 * 주어진 key의 context를 캐시에서 제거하고 명시적으로 {@linkplain ConfigurableApplicationContext#close() 종료함}.
	 *
	 * <p>일반적으로 싱글턴 빈의 상태를 바꾸면(잠재적으로 context의 미래 interaction에 영향을 끼치는) 이메서드만 호출하면 됨.</p>
	 * <p>추가로, {@code HierarchyMode}를 준수함. Javadoc으로 {@link HierarchyMode}의 상세사항을 참고할것. </p>
	 *
	 * @param key context key (절대 {@code null}이면 안됨)
	 * @param hierarchyMode hierarchy mode; context가 구조를 이루지 않는다면 {@code null} 가능
	 */
	public void remove(MergedContextConfiguration key, HierarchyMode hierarchyMode) {
		Assert.notNull(key, "Key must not be null");

		// startKey는 캐시 clear를 시작할 level로, hierarchy mode 설정에 의존함.
		MergedContextConfiguration startKey = key;
		if (hierarchyMode == HierarchyMode.EXHAUSTIVE) {
			while (startKey.getParent() != null) {
				startKey = startKey.getParent();
			}
		}

		List<MergedContextConfiguration> removedContexts = new ArrayList<MergedContextConfiguration>();
		remove(removedContexts, startKey);

		// 삭제된 context를 가리키는 모든 남아있는 reference를 hierarchy map에서 지움.
		for (MergedContextConfiguration currentKey : removedContexts) {
			for (Set<MergedContextConfiguration> children : this.hierarchyMap.values()) {
				children.remove(currentKey);
			}
		}

		// hierarchy map에서 빈 엔트리들을 지움.
		for (MergedContextConfiguration currentKey : this.hierarchyMap.keySet()) {
			if (this.hierarchyMap.get(currentKey).isEmpty()) {
				this.hierarchyMap.remove(currentKey);
			}
		}
	}

	private void remove(List<MergedContextConfiguration> removedContexts, MergedContextConfiguration key) {
		Assert.notNull(key, "Key must not be null");

		Set<MergedContextConfiguration> children = this.hierarchyMap.get(key);
		if (children != null) {
			for (MergedContextConfiguration child : children) {
				// low level로 재귀호출
				remove(removedContexts, child);
			}
			// hierarchy map으로 부터 현재 context의 자식 set을 제거
			this.hierarchyMap.remove(key);
		}

		// 물리적으로 leaft 노드들을 먼저 제거하고 종료
		ApplicationContext context = this.contextMap.remove(key);
		if (context instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) context).close();
		}
		removedContexts.add(key);
	}

	/**
	 * 현재 저장되어 있는 캐시의 컨텍스트의 개수를 리턴.
	 * <p>만약 캐시가 {@code Integer.MAX_VALUE} 이상의 개수를 보유하면, 이 메서드는 {@code Integer.MAX_VALUE}를 리턴함.</p>
	 */
	public int size() {
		return this.contextMap.size();
	}

	/**
	 * 캐시 안에서 현재 추적되는 부모 컨텍스트의 개수를 리턴.
	 */
	public int getParentContextCount() {
		return this.hierarchyMap.size();
	}

	/**
	 * 아래의 정보를 포함하여 toString 생성.
	 * {@linkplain #size}, @linkplain #getHitCount() hit}, {@linkplain #getMissCount() miss},
	 * {@linkplain #getParentContextCount() parent context} 카운트.
	 */
	@Override
	public String toString() {
		return new ToStringCreator(this)
				.append("size", size())
				.append("hitCount", getHitCount())
				.append("missCount", getMissCount())
				.append("parentContextCount", getParentContextCount())
				.toString();
	}

}
