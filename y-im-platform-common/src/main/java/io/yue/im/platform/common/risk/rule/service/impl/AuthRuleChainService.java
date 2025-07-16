package io.yue.im.platform.common.risk.rule.service.impl;

import io.yue.im.platform.common.exception.IMException;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.enums.HttpCode;
import io.yue.im.platform.common.risk.enums.RuleEnum;
import io.yue.im.platform.common.risk.rule.service.RuleChainService;
import io.yue.im.platform.common.risk.rule.service.base.BaseRuleChainService;
import io.yue.im.platform.common.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * @description 账号安全校验
 */
@Component
public class AuthRuleChainService extends BaseRuleChainService implements RuleChainService {
    private final Logger logger = LoggerFactory.getLogger(AuthRuleChainService.class);

    @Value("${bh.im.rule.authRule.order}")
    private Integer authRuleOrder;

    @Override
    public HttpCode execute(HttpServletRequest request, Object handler) {
        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return HttpCode.SUCCESS;
        }
        UserSession userSession = this.getUserSession(request);
        if (userSession == null){
            logger.error("AuthRuleChainService|未登录，url|{}",request.getRequestURI());
            throw new IMException(HttpCode.NO_LOGIN);
        }
        request.setAttribute(IMPlatformConstants.SESSION, userSession);
        return HttpCode.SUCCESS;
    }

    @Override
    public int getOrder() {
        return authRuleOrder == null ? RuleEnum.AUTH.getCode() : authRuleOrder;
    }

    @Override
    public String getServiceName() {
        return RuleEnum.AUTH.getMesaage();
    }
}
