package io.yue.im.platform.user.application.dubbo;

import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.entity.User;
import io.yue.im.platform.dubbo.user.UserDubboService;
import io.yue.im.platform.user.application.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description 用户的Dubbo服务实现
 */
@Component
@DubboService(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION)
public class UserDubboServiceImpl implements UserDubboService {
    @Autowired
    private UserService userService;

    @Override
    public User getUserById(Long userId) {
        return userService.getUserById(userId);
    }
}
