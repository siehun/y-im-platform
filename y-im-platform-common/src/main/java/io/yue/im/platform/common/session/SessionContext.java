package io.yue.im.platform.common.session;

import io.yue.im.platform.common.exception.IMException;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.enums.HttpCode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @description Session上下文
 */
public class SessionContext {

    public static UserSession getSession(){
        // 从请求上下文里获取Request对象
        ServletRequestAttributes requestAttributes = ServletRequestAttributes.class.
                cast(RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = requestAttributes.getRequest();
        Object object = request.getAttribute(IMPlatformConstants.SESSION);
        if (object == null){
            throw new IMException(HttpCode.NO_LOGIN);
        }
        return  (UserSession) object;
    }
}
