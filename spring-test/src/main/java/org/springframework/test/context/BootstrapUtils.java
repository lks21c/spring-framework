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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;

import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * {@code BootstrapUtils}은 유틸리티 메서드의 집합으로 <em>Spring TestContext Framework</em>를 로딩하는것을 도와줌.
 *
 * @author Sam Brannen
 * @since 4.1
 * @see BootstrapWith
 * @see BootstrapContext
 * @see TestContextBootstrapper
 */
abstract class BootstrapUtils {

	private static final String DEFAULT_TEST_CONTEXT_BOOTSTRAPPER_CLASS_NAME = "org.springframework.test.context.support.DefaultTestContextBootstrapper";

	private static final Log logger = LogFactory.getLog(BootstrapUtils.class);


	private BootstrapUtils() {
		/* no-op */
	}

	/**
	 * {@link BootstrapContext} 안의 테스트 클래스를 위한 {@link TestContextBootstrapper} 타입을 reslove하고,
	 * 인스턴스화 하고, 레퍼런스를 {@link BootstrapContext}에 제공함.
	 *
	 * <p>만약 {@link BootstrapWith @BootstrapWith} 어노테이션에 테스트 클래스에 있으면(직접적으로나 메타 어노테이션으로),
	 * {@link BootstrapWith#value value}은 bootstrapper 타입으로 쓰여짐.
	 * 그렇지 않을경우, {@link org.springframework.test.context.support.DefaultTestContextBootstrapper
	 * DefaultTestContextBootstrapper}이 쓰여짐.
	 * </p>
	 *
	 * @param bootstrapContext 사용할 bootstrap context
	 * @return 완전히 설정된 {@code TestContextBootstrapper}
	 */
	@SuppressWarnings("unchecked")
	static TestContextBootstrapper resolveTestContextBootstrapper(BootstrapContext bootstrapContext) {
		Class<?> testClass = bootstrapContext.getTestClass();

		Class<? extends TestContextBootstrapper> clazz = null;
		try {
			BootstrapWith bootstrapWith = findAnnotation(testClass, BootstrapWith.class);
			if (bootstrapWith != null && !TestContextBootstrapper.class.equals(bootstrapWith.value())) {
				clazz = bootstrapWith.value();
			}
			else {
				clazz = (Class<? extends TestContextBootstrapper>) ClassUtils.forName(
					DEFAULT_TEST_CONTEXT_BOOTSTRAPPER_CLASS_NAME, BootstrapUtils.class.getClassLoader());
			}

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Instantiating TestContextBootstrapper from class [%s]", clazz.getName()));
			}

			TestContextBootstrapper testContextBootstrapper = instantiateClass(clazz, TestContextBootstrapper.class);
			testContextBootstrapper.setBootstrapContext(bootstrapContext);

			return testContextBootstrapper;
		}
		catch (Throwable t) {
			throw new IllegalStateException("Could not load TestContextBootstrapper [" + clazz
					+ "]. Specify @BootstrapWith's 'value' attribute "
					+ "or make the default bootstrapper class available.", t);
		}
	}

}
