package io.yue.im.platform.common.interceptor;

import io.yue.im.platform.common.exception.IMException;
import io.yue.im.platform.common.interceptor.base.BaseInterceptor;
import io.yue.im.platform.common.model.enums.HttpCode;
import io.yue.im.platform.common.risk.rule.service.RuleChainService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description 大后端平台统一拦截器
 */
@Component
public class IMInterceptor extends BaseInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取排好序的规则链
        List<RuleChainService> ruleChainServices = this.getRuleChainServices();
        //遍历规则链
        for (RuleChainService ruleChainService : ruleChainServices){
            HttpCode httpCode = ruleChainService.execute(request, handler);
            if (!HttpCode.SUCCESS.getCode().equals(httpCode.getCode())){
                 throw new IMException(httpCode);
            }
        }
        return true;
    }
}
