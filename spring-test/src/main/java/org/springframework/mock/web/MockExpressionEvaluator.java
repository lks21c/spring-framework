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

package org.springframework.mock.web;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * JSP 2.0의 Mock 구현
 * {@link javax.servlet.jsp.el.ExpressionEvaluator} 인터페이스를 구현하여,
 * Jakarta JSTL ExpressionEvaluatorManager로 전달함.
 * <p>
 * <p>
 * web framework테스트에 사용함; custom JSP 태그를 포함한 어플리케이션 테스트에서만 필요함.
 * </p>
 *
 * <p>
 * 노트 : 이 expression evaluator를 쓰기 위해 Jakarta JSTL 구현(jstl.jar, standard.jar)이 class path에서 사용가능해야함.
 * </p>
 * <p>
 *
 * @author Juergen Hoeller
 * @see org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager
 * @since 1.1.5
 */
@SuppressWarnings("deprecation")
public class MockExpressionEvaluator extends javax.servlet.jsp.el.ExpressionEvaluator {

    private final PageContext pageContext;


    /**
     * 새 MockExpressionEvaluator를 주어진 PageContext로 생성함.
     *
     * @param pageContext 실행할 JSP PageContext
     */
    public MockExpressionEvaluator(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public javax.servlet.jsp.el.Expression parseExpression(final String expression, final Class expectedType,
                                                           final javax.servlet.jsp.el.FunctionMapper functionMapper) throws javax.servlet.jsp.el.ELException {

        return new javax.servlet.jsp.el.Expression() {

            @Override
            public Object evaluate(javax.servlet.jsp.el.VariableResolver variableResolver) throws javax.servlet.jsp.el.ELException {
                return doEvaluate(expression, expectedType, functionMapper);
            }
        };
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object evaluate(String expression, Class expectedType, javax.servlet.jsp.el.VariableResolver variableResolver,
                           javax.servlet.jsp.el.FunctionMapper functionMapper) throws javax.servlet.jsp.el.ELException {

        if (variableResolver != null) {
            throw new IllegalArgumentException("Custom VariableResolver not supported");
        }
        return doEvaluate(expression, expectedType, functionMapper);
    }

    @SuppressWarnings("rawtypes")
    protected Object doEvaluate(String expression, Class expectedType, javax.servlet.jsp.el.FunctionMapper functionMapper)
            throws javax.servlet.jsp.el.ELException {

        if (functionMapper != null) {
            throw new IllegalArgumentException("Custom FunctionMapper not supported");
        }
        try {
            return ExpressionEvaluatorManager.evaluate("JSP EL expression", expression, expectedType, this.pageContext);
        } catch (JspException ex) {
            throw new javax.servlet.jsp.el.ELException("Parsing of JSP EL expression \"" + expression + "\" failed", ex);
        }
    }

}
