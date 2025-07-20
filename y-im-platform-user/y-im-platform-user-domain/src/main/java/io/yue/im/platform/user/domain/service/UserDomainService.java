package io.yue.im.platform.user.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.yue.im.platform.common.model.entity.User;

import java.util.List;

/**
 * @description 领域层用户Service
 */
public interface UserDomainService extends IService<User> {

    /**
     * 根据用户名获取用户信息
     */
    User getUserByUserName(String userName);

    /**
     * 保存用户
     */
    boolean saveOrUpdateUser(User user);

    /**
     * 根据名称模糊查询用户列表
     */
    List<User> getUserListByName(String name);

    /**
     * 根据id获取用户数据
     */
    User getById(Long userId);

    /**
     * 模糊查询用户
     */
    List<User> findUserByName(String name);
}
