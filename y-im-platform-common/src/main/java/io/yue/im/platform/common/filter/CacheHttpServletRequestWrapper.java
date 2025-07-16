package io.yue.im.platform.common.filter;


import cn.hutool.core.io.IoUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * @description 带缓存的HttpServletRequest
 */
public class CacheHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private byte[] requestBody;
    private final HttpServletRequest request;

    public CacheHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.request = request;
    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (null == this.requestBody) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IoUtil.copy(request.getInputStream(), baos);
            this.requestBody = baos.toByteArray();
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(requestBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() {
                return bais.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }
}
