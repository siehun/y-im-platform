package io.yue.im.platform.dubbo.user;

import io.yue.im.platform.common.model.entity.User;

public interface UserDubboService {
    /**
     * 根据用户id获取用户实体对象
     * @param userId
     * @return
     */
     User getUserById(Long userId);
}
