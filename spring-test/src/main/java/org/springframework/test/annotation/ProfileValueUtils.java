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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * <em>profile values</em>를 위한 일반적인 유틸리티 메서드들.
 *
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @see ProfileValueSource
 * @see ProfileValueSourceConfiguration
 * @see IfProfileValue
 * @since 2.5
 */
public abstract class ProfileValueUtils {

    private static final Log logger = LogFactory.getLog(ProfileValueUtils.class);

    /**
     * 명시된 {@link Class test class}에서
     * {@link ProfileValueSourceConfiguration
     * &#064;ProfileValueSourceConfiguration} 어노테이션을 통해서
     * {@link ProfileValueSource}을 얻고 새 인스턴스를 생성함.
     * <p/>
     * <p/>
     * 만약 {@link ProfileValueSourceConfiguration
     * &#064;ProfileValueSourceConfiguration}이 명시된 클래스에 존재하지 않거나 커스텀 {@link ProfileValueSource}가
     * 정의되지 않으면 기본 {@link SystemProfileValueSource}이 대신에 리턴됨.
     *
     * @param testClass ProfileValueSource가 얻어질 테스트 클래스
     * @return 설정된(또는 기본의) ProfileValueSource 클래스
     * @see SystemProfileValueSource
     */
    @SuppressWarnings("unchecked")
    public static ProfileValueSource retrieveProfileValueSource(Class<?> testClass) {
        Assert.notNull(testClass, "testClass must not be null");

        Class<ProfileValueSourceConfiguration> annotationType = ProfileValueSourceConfiguration.class;
        ProfileValueSourceConfiguration config = findAnnotation(testClass, annotationType);
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieved @ProfileValueSourceConfiguration [" + config + "] for test class ["
                    + testClass.getName() + "]");
        }

        Class<? extends ProfileValueSource> profileValueSourceType;
        if (config != null) {
            profileValueSourceType = config.value();
        } else {
            profileValueSourceType = (Class<? extends ProfileValueSource>) AnnotationUtils.getDefaultValue(annotationType);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieved ProfileValueSource type [" + profileValueSourceType + "] for class ["
                    + testClass.getName() + "]");
        }

        ProfileValueSource profileValueSource;
        if (SystemProfileValueSource.class.equals(profileValueSourceType)) {
            profileValueSource = SystemProfileValueSource.getInstance();
        } else {
            try {
                profileValueSource = profileValueSourceType.newInstance();
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Could not instantiate a ProfileValueSource of type [" + profileValueSourceType
                            + "] for class [" + testClass.getName() + "]: using default.", e);
                }
                profileValueSource = SystemProfileValueSource.getInstance();
            }
        }

        return profileValueSource;
    }

    /**
     * Determine if the supplied {@code testClass} is <em>enabled</em> in
     * the current environment, as specified by the {@link IfProfileValue
     * &#064;IfProfileValue} annotation at the class level.
     * <p/>
     * Defaults to {@code true} if no {@link IfProfileValue
     * &#064;IfProfileValue} annotation is declared.
     *
     * @param testClass the test class
     * @return {@code true} if the test is <em>enabled</em> in the current
     * environment
     */
    public static boolean isTestEnabledInThisEnvironment(Class<?> testClass) {
        IfProfileValue ifProfileValue = findAnnotation(testClass, IfProfileValue.class);
        return isTestEnabledInThisEnvironment(retrieveProfileValueSource(testClass), ifProfileValue);
    }

    /**
     * {@code testMethod}가 현재의 환경에 enable되어있는지 확인.
     * 테스트 메서드나 클래스에 정의된 {@link IfProfileValue
     * &#064;IfProfileValue}을 확인함. 클래스 레벨 사용은 메서드 레벨 사용을 overridde 함.
     * <p/>
     * <p/>
     * 정의된 {@link IfProfileValue
     * &#064;IfProfileValue} 어노테이션이 없으면 {@code true}를 리턴.
     *
     * @param testMethod 테스트 메소드
     * @param testClass  테스트 클래스
     * @return 만약 현재의 환경에서 test가 enable되어 있으면 {@code true}
     */
    public static boolean isTestEnabledInThisEnvironment(Method testMethod, Class<?> testClass) {
        return isTestEnabledInThisEnvironment(retrieveProfileValueSource(testClass), testMethod, testClass);
    }

    /**
     * {@code testMethod}가 현재의 환경에 enable되어있는지 확인.
     * 테스트 메서드나 클래스에 정의된 {@link IfProfileValue
     * &#064;IfProfileValue}을 확인함. 클래스 레벨 사용은 메서드 레벨 사용을 overridde 함.
     * <p/>
     * <p/>
     * 정의된 {@link IfProfileValue
     * &#064;IfProfileValue} 어노테이션이 없으면 {@code true}를 리턴.
     *
     * @param profileValueSource 테스트가 enable 되어 있는지 확인할 ProfileValueSource
     * @param testMethod         테스트 메소드
     * @param testClass          테스트 클래스
     * @return 만약 현재의 환경에서 test가 enable되어 있으면 {@code true}
     */
    public static boolean isTestEnabledInThisEnvironment(ProfileValueSource profileValueSource, Method testMethod,
                                                         Class<?> testClass) {

        IfProfileValue ifProfileValue = findAnnotation(testClass, IfProfileValue.class);
        boolean classLevelEnabled = isTestEnabledInThisEnvironment(profileValueSource, ifProfileValue);

        if (classLevelEnabled) {
            ifProfileValue = findAnnotation(testMethod, IfProfileValue.class);
            return isTestEnabledInThisEnvironment(profileValueSource, ifProfileValue);
        }

        return false;
    }

    /**
     * {@link IfProfileValue &#064;IfProfileValue}으로 얻어진 {@code value}(또는 {@code value} 중 하나)가
     * Determine if the {@code value} (or one of the {@code values})이 현재의 환경에 enable 되어 있는지
     * 여부를 리턴.
     *
     * @param profileValueSource 테스트가 enable 되어 있는지 확인할 ProfileValueSource
     * @param ifProfileValue     조사할 어노테이션;{@code null} 일수 있음
     * @return 만약 현재의 환경에서 test가 enable되어 있거나 {@code ifProfileValue}가 {@code null}이면 {@code true}
     */
    private static boolean isTestEnabledInThisEnvironment(ProfileValueSource profileValueSource,
                                                          IfProfileValue ifProfileValue) {

        if (ifProfileValue == null) {
            return true;
        }

        String environmentValue = profileValueSource.get(ifProfileValue.name());
        String[] annotatedValues = ifProfileValue.values();
        if (StringUtils.hasLength(ifProfileValue.value())) {
            if (annotatedValues.length > 0) {
                throw new IllegalArgumentException("Setting both the 'value' and 'values' attributes "
                        + "of @IfProfileValue is not allowed: choose one or the other.");
            }
            annotatedValues = new String[]{ifProfileValue.value()};
        }

        for (String value : annotatedValues) {
            if (ObjectUtils.nullSafeEquals(value, environmentValue)) {
                return true;
            }
        }
        return false;
    }

}
