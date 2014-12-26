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

package org.springframework.mock.http.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.MockHttpOutputMessage;

import java.io.IOException;
import java.net.URI;

/**
 * {@link ClientHttpRequest}의 Mock 구현.
 *
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @since 3.2
 */
public class MockClientHttpRequest extends MockHttpOutputMessage implements ClientHttpRequest {

    private URI uri;

    private HttpMethod httpMethod;

    private boolean executed = false;

    private ClientHttpResponse clientHttpResponse;


    /**
     * 기본 생성자.
     */
    public MockClientHttpRequest() {
    }

    /**
     * 주어진 HttpMethod 와 URI로 생성.
     */
    public MockClientHttpRequest(HttpMethod httpMethod, URI uri) {
        this.httpMethod = httpMethod;
        this.uri = uri;
    }

    @Override
    public URI getURI() {
        return this.uri;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    @Override
    public HttpMethod getMethod() {
        return this.httpMethod;
    }

    public void setMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setResponse(ClientHttpResponse clientHttpResponse) {
        this.clientHttpResponse = clientHttpResponse;
    }

    public boolean isExecuted() {
        return this.executed;
    }

    /**
     * {@link #isExecuted() executed} 플래그를 {@code true}로 설정 <br />
     * {@link #setResponse(ClientHttpResponse) response}를 리턴.
     *
     * @see #executeInternal()
     */
    @Override
    public final ClientHttpResponse execute() throws IOException {
        this.executed = true;
        return executeInternal();
    }

    /**
     * 기본 구현은 {@link #setResponse(ClientHttpResponse) response}로 설정된 값을 리턴.
     * <p>
     * <p>request를 실행하고 response를 제공해 주기 위해 이 메서드를 오버라이드 필요.
     * 잠재적으로 설정된 response와 다름.
     */
    protected ClientHttpResponse executeInternal() throws IOException {
        return this.clientHttpResponse;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.httpMethod != null) {
            sb.append(this.httpMethod);
        }
        if (this.uri != null) {
            sb.append(" ").append(this.uri);
        }
        if (!getHeaders().isEmpty()) {
            sb.append(", headers : ").append(getHeaders());
        }
        if (sb.length() == 0) {
            sb.append("Not yet initialized");
        }
        return sb.toString();
    }

}
