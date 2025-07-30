package io.yue.im.platform.group.application.dubbo;

import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.entity.Group;
import io.yue.im.platform.common.model.params.GroupParams;
import io.yue.im.platform.common.model.vo.GroupMemberSimpleVO;
import io.yue.im.platform.dubbo.group.GroupDubboService;
import io.yue.im.platform.group.application.service.GroupService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @description 群组Dubbo
 */
@Component
@DubboService(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION)
public class GroupDubboServiceImpl implements GroupDubboService {

    @Autowired
    private GroupService groupService;
    @Override
    public boolean isExists(Long groupId) {
        Group group = groupService.getById(groupId);
        if (Objects.isNull(group)) {
            return false;
        }
        if (group.getDeleted()) {
            return false;
        }
        return true;
    }

    @Override
    public GroupMemberSimpleVO getGroupMemberSimpleVO(GroupParams groupParams) {
        return groupService.getGroupMemberSimpleVO(groupParams);
    }

    @Override
    public List<Long> getUserIdsByGroupId(Long groupId) {
        return groupService.getUserIdsByGroupId(groupId);
    }

    @Override
    public List<Long> getGroupIdsByUserId(Long userId) {
        return groupService.getGroupIdsByUserId(userId);
    }

    @Override
    public List<GroupMemberSimpleVO> getGroupMemberSimpleVOList(Long userId) {
        return groupService.getGroupMemberSimpleVOList(userId);
    }
}
