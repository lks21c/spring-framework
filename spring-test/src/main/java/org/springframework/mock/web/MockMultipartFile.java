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

import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link org.springframework.web.multipart.MultipartFile} 인터페이스의 mock 구현.
 * <p>
 * <p>
 * {@link MockMultipartHttpServletRequest}와의 조합하여 multipart 업로드에 접근하는 어플리케이션 컨트롤러
 * 테스트에 유용함.
 * </p>
 *
 * @author Juergen Hoeller
 * @author Eric Crampton
 * @see MockMultipartHttpServletRequest
 * @since 2.0
 */
public class MockMultipartFile implements MultipartFile {

    private final String name;

    private String originalFilename;

    private String contentType;

    private final byte[] content;


    /**
     * 주어진 content로 MockMultipartFile을 생성함.
     *
     * @param name    주어진 파일이름
     * @param content content 파일
     */
    public MockMultipartFile(String name, byte[] content) {
        this(name, "", null, content);
    }

    /**
     * 주어진 content로 MockMultipartFile을 생성함.
     *
     * @param name    주어진 파일이름
     * @param contentStream {@code InputStream} 형태의 content 파일
     * @throws IOException stream 읽기에 실패 시 발생
     */
    public MockMultipartFile(String name, InputStream contentStream) throws IOException {
        this(name, "", null, FileCopyUtils.copyToByteArray(contentStream));
    }

    /**
     * 주어진 content로 MockMultipartFile을 생성함.
     *
     * @param name    주어진 파일이름
     * @param originalFilename 원본 파일 이름(클라이언트 머신에 따라)
     * @param contentType      컨텐트 타입(알려져 있으면)
     * @param content content 파일
     */
    public MockMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        Assert.hasLength(name, "Name must not be null");
        this.name = name;
        this.originalFilename = (originalFilename != null ? originalFilename : "");
        this.contentType = contentType;
        this.content = (content != null ? content : new byte[0]);
    }

    /**
     * 주어진 content로 MockMultipartFile을 생성함.
     *
     * @param name    주어진 파일이름
     * @param originalFilename 원본 파일 이름(클라이언트 머신에 따라)
     * @param contentType      컨텐트 타입(알려져 있으면)
     * @param contentStream {@code InputStream} 형태의 content 파일
     * @throws IOException stream 읽기에 실패 시 발생
     */
    public MockMultipartFile(String name, String originalFilename, String contentType, InputStream contentStream)
            throws IOException {

        this(name, originalFilename, contentType, FileCopyUtils.copyToByteArray(contentStream));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return (this.content.length == 0);
    }

    @Override
    public long getSize() {
        return this.content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return this.content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        FileCopyUtils.copy(this.content, dest);
    }

}
