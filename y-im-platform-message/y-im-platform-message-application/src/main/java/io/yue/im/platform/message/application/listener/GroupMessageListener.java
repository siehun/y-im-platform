package io.yue.im.platform.message.application.listener;

import io.yue.im.common.cache.distribute.DistributedCacheService;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.common.domain.enums.IMListenerType;
import io.yue.im.common.domain.enums.IMSendCode;
import io.yue.im.common.domain.model.IMSendResult;
import io.yue.im.platform.common.model.vo.GroupMessageVO;
import io.yue.im.sdk.domain.annotation.IMListener;
import io.yue.im.sdk.domain.listener.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @description 监听群消息
 */
@IMListener(listenerType = IMListenerType.GROUP_MESSAGE)
public class GroupMessageListener implements MessageListener<GroupMessageVO> {
    @Autowired
    private DistributedCacheService distributedCacheService;
    @Override
    public void doProcess(IMSendResult<GroupMessageVO> result) {
        GroupMessageVO messageInfo = result.getData();
        if (IMSendCode.SUCCESS.code().equals(result.getCode())){
            String redisKey = String.join(IMConstants.REDIS_KEY_SPLIT, IMConstants.IM_GROUP_READED_POSITION, messageInfo.getGroupId().toString(), result.getReceiver().getUserId().toString());
            distributedCacheService.set(redisKey, messageInfo.getId());
        }
    }
}
