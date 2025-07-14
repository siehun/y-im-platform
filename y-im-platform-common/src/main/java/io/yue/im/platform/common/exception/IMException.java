package io.yue.im.platform.common.exception;

import io.yue.im.platform.common.model.enums.HttpCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IMException extends RuntimeException {
    private static final long serialVersionUID = -2571805513813090624L;
    private Integer code;
    private String message;
    public IMException(HttpCode httpCode, String message) {
        this.code = httpCode.getCode();
        this.message = message;
    }
    public IMException(HttpCode httpCode)  {
        this.code = httpCode.getCode();
        this.message = httpCode.getMsg();
    }

    public IMException(String message) {
        this.code = HttpCode.PROGRAM_ERROR.getCode();
        this.message = message;
    }
}
