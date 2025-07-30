package io.yue.im.platform.friend.application.dubbo;

import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.entity.Friend;
import io.yue.im.platform.dubbo.friend.FriendDubboService;
import io.yue.im.platform.friend.application.service.FriendService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description 用户服务实现类
 */
@Component
@DubboService(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION)
public class FriendDubboServiceImpl implements FriendDubboService {
    @Autowired
    private FriendService friendService;

    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        return friendService.isFriend(userId1, userId2);
    }

    @Override
    public List<Long> getFriendIdList(Long userId) {
        return friendService.getFriendIdList(userId);
    }

    @Override
    public List<Friend> getFriendByUserId(Long userId) {
        return friendService.getFriendByUserId(userId);
    }
}
