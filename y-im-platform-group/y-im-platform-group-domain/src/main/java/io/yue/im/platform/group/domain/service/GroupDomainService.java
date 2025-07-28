package io.yue.im.platform.group.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.yue.im.platform.common.model.entity.Group;
import io.yue.im.platform.common.model.params.GroupParams;
import io.yue.im.platform.common.model.vo.GroupVO;

import java.util.List;

/**
 * @description 群组领域服务
 */
public interface GroupDomainService extends IService<Group> {
    /**
     * 创建群组
     */
    GroupVO createGroup(GroupVO vo, Long userId);

    /**
     * 更新群组信息
     */
    GroupVO modifyGroup(GroupVO vo, Long userId);

    /**
     * 删除群组
     */
    boolean deleteGroup(Long groupId, Long userId);

    /**
     * 退群
     */
    boolean quitGroup(Long groupId, Long userId);

    /**
     * 踢人出群
     */
    boolean kickGroup(Long groupId, Long kickUserId, Long userId);

    /**
     * 根据id获取群组信息
     */
    GroupVO getGroupVOById(Long groupId, Long userId);

    /**
     * 根据参数获取群组信息
     */
    GroupVO getGroupVOByParams(GroupParams params);

    /**
     * 根据id获取群组信息
     */
    Group getGroupById(Long groupId);

    /**
     * 获取用户所在的群组
     */
    List<GroupVO> getGroupVOListByUserId(Long userId);

    /**
     * 获取群组名称
     */
    String getGroupName(Long groupId);

    /**
     * 群组是够存在
     */
    boolean isExists(Long groupId);
}
