package io.yue.im.platform.common.interceptor.base;

import cn.hutool.core.collection.CollectionUtil;
import io.yue.im.platform.common.risk.rule.service.RuleChainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description 基础拦截器
 */
public abstract class BaseInterceptor implements HandlerInterceptor {

    @Autowired
    private List<RuleChainService> ruleChainServices;

    protected List<RuleChainService> getRuleChainServices(){
        if (CollectionUtil.isEmpty(ruleChainServices)){
            return Collections.emptyList();
        }
        return ruleChainServices.stream().sorted(Comparator.comparing(RuleChainService::getOrder)).collect(Collectors.toList());
    }
}
