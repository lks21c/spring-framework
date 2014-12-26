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

package org.springframework.mock.jndi;

import org.springframework.jndi.JndiTemplate;

import javax.naming.NamingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 항상 주어진 object를 리턴하는 JndiTemplate의 간단한 확장
 *
 * <p>테스팅을 위해 매우 유용함. 효과적인 mock object.</p>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class ExpectedLookupTemplate extends JndiTemplate {

	private final Map<String, Object> jndiObjects = new ConcurrentHashMap<String, Object>(16);


	/**
	 * 항상 주어진 object를 리턴하는 JndiTemplate를 생성함.
	 * 사용하려면 {@code addObject} 호출이 필요함.
	 * @see #addObject(String, Object)
	 */
	public ExpectedLookupTemplate() {
	}

	/**
	 * 항상 주어진 object를 리턴하는 새 JndiTemplate를 생성함.
	 * 하지만, 주어진 이름의 request만 준수함.
	 *
	 * @param name 클라이언트가 look up하기를 기대하는 이름
	 * @param object 리턴 될 object
	 */
	public ExpectedLookupTemplate(String name, Object object) {
		addObject(name, object);
	}

	/**
	 * Add the given object to the list of JNDI objects that this template will expose.
	 * @param name the name the client is expected to look up
	 * @param object the object that will be returned
	 */
	public void addObject(String name, Object object) {
		this.jndiObjects.put(name, object);
	}

	/**
	 * 만약 파라미터의 이름이 생성자에서 지정한 이름이라면, 이미 생성자에서 지정한 object를 리턴함.
	 * 만약 이름을 못찾으면 NamingException이 발생함.
	 *
	 */
	@Override
	public Object lookup(String name) throws NamingException {
		Object object = this.jndiObjects.get(name);
		if (object == null) {
			throw new NamingException("Unexpected JNDI name '" + name + "': expecting " + this.jndiObjects.keySet());
		}
		return object;
	}

}
