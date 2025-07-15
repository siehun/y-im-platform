package io.yue.im.platform.common.risk.rule.service;


import io.yue.im.platform.common.model.enums.HttpCode;

import javax.servlet.http.HttpServletRequest;

/**
 * @description IM大后端平台的规则调用链接口
 */
public interface RuleChainService {

    /**
     * 执行处理逻辑
     */
    HttpCode execute(HttpServletRequest request, Object handler);

    /**
     * 规则链中的每个规则排序
     */
    int getOrder();
}
