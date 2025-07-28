package io.yue.im.platform.group.application.service;


import io.yue.im.platform.common.model.entity.Group;
import io.yue.im.platform.common.model.params.GroupParams;
import io.yue.im.platform.common.model.vo.GroupMemberSimpleVO;
import io.yue.im.platform.common.model.vo.GroupMemberVO;
import io.yue.im.platform.common.model.vo.GroupVO;
import io.yue.im.platform.common.model.vo.GroupInviteVO;

import java.util.List;

/**
 * @description 群组服务
 */
public interface GroupService {

    /**
     * 创建新群聊
     * @param  vo 群聊信息
     * @return 群聊信息
     **/
    GroupVO createGroup(GroupVO vo);

    /**
     * 修改群聊信息
     * @param  vo 群聊信息
     * @return 群聊信息
     **/
    GroupVO modifyGroup(GroupVO vo);

    /**
     * 删除群聊
     * @param groupId 群聊id
     **/
    void deleteGroup(Long groupId);

    /**
     * 退出群聊
     * @param groupId 群聊id
     */
    void quitGroup(Long groupId);

    /**
     * 将用户踢出群聊
     * @param groupId 群聊id
     * @param userId 用户id
     */
    void kickGroup(Long groupId,Long userId);

    /**
     * 查询当前用户的所有群聊
     * @return 群聊信息列表
     **/
    List<GroupVO> findGroups();

    /**
     * 邀请好友进群
     * @param vo  群id、好友id列表
     **/
    void invite(GroupInviteVO vo);

    /**
     * 根据id查找群聊，并进行缓存
     * @param groupId 群聊id
     * @return 群聊实体
     */
    Group getById(Long groupId);

    /**
     * 根据id查找群聊
     * @param groupId 群聊id
     * @return 群聊vo
     */
    GroupVO findById(Long groupId);

    /**
     * 查询群成员
     * @param groupId 群聊id
     * @return List<GroupMemberVO>
     **/
    List<GroupMemberVO> findGroupMembers(Long groupId);

    /**
     * 获取群成员群备注昵称和是否退出的状态
     */
    GroupMemberSimpleVO getGroupMemberSimpleVO(GroupParams groupParams);

    /**
     * 获取群成员id列表
     */
    List<Long> getUserIdsByGroupId(Long groupId);

    /**
     * 根据用户id拉取群组id列表
     */
    List<Long> getGroupIdsByUserId(Long userId);

    /**
     * 根据用户id获取在各个群组中的信息
     */
    List<GroupMemberSimpleVO> getGroupMemberSimpleVOList(Long userId);

    /**
     * 更新某个用户在所有群的头像
     */
    boolean updateHeadImgByUserId(String headImg, Long userId);
}
