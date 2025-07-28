package io.yue.im.platform.group.application.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.yue.im.common.cache.distribute.DistributedCacheService;
import io.yue.im.common.cache.id.SnowFlakeFactory;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.common.mq.event.MessageEventSenderService;
import io.yue.im.platform.common.exception.IMException;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.entity.Friend;
import io.yue.im.platform.common.model.entity.Group;
import io.yue.im.platform.common.model.entity.GroupMember;
import io.yue.im.platform.common.model.entity.User;
import io.yue.im.platform.common.model.enums.HttpCode;
import io.yue.im.platform.common.model.params.GroupParams;
import io.yue.im.platform.common.model.vo.GroupInviteVO;
import io.yue.im.platform.common.model.vo.GroupMemberSimpleVO;
import io.yue.im.platform.common.model.vo.GroupMemberVO;
import io.yue.im.platform.common.model.vo.GroupVO;
import io.yue.im.platform.common.session.SessionContext;
import io.yue.im.platform.common.session.UserSession;
import io.yue.im.platform.dubbo.friend.FriendDubboService;
import io.yue.im.platform.dubbo.user.UserDubboService;
import io.yue.im.platform.group.application.service.GroupService;
import io.yue.im.platform.group.domain.event.IMGroupEvent;
import io.yue.im.platform.group.domain.service.GroupDomainService;
import io.yue.im.platform.group.domain.service.GroupMemberDomainService;
import io.yue.im.sdk.client.IMClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description 群组服务实现类
 */
@Service
@CacheConfig(cacheNames = IMConstants.IM_CACHE_GROUP)
public class GroupServiceImpl implements GroupService {
    private final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    @Autowired
    private IMClient imClient;
    @Value("${message.mq.event.type}")
    private String eventType;
    @Autowired
    private GroupDomainService groupDomainService;
    @Autowired
    private DistributedCacheService distributedCacheService;
    @Autowired
    private GroupMemberDomainService groupMemberDomainService;
    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private UserDubboService userDubboService;
    @DubboReference(version = IMPlatformConstants.DEFAULT_DUBBO_VERSION, check = false)
    private FriendDubboService friendDubboService;
    @Autowired
    private MessageEventSenderService messageEventSenderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupVO createGroup(GroupVO vo) {
        if (vo == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        User user = userDubboService.getUserById(session.getUserId());
        if (user == null){
            throw new IMException(HttpCode.PROGRAM_ERROR, "未获取到用户信息");
        }
        vo =  this.getGroupVO(groupDomainService.createGroup(vo, session.getUserId()), session, user);
        logger.info("创建群聊，群聊id:{},群聊名称:{}", vo.getId(), vo.getName());
        //TODO 发送异步事件
        IMGroupEvent imGroupEvent = new IMGroupEvent(vo.getId(), user.getId(), IMPlatformConstants.GROUP_HANDLER_CREATE, this.getTopicEvent());
        messageEventSenderService.send(imGroupEvent);
        return vo;
    }

    private GroupVO getGroupVO(GroupVO vo, UserSession session, User user) {
        // 把群主加入群
        GroupMember groupMember = new GroupMember();
        groupMember.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId());
        groupMember.setGroupId(vo.getId());
        groupMember.setUserId(user.getId());
        groupMember.setHeadImage(user.getHeadImageThumb());
        groupMember.setAliasName(StringUtils.isEmpty(vo.getAliasName()) ? session.getNickName() : vo.getAliasName());
        groupMember.setRemark(vo.getRemark());
        groupMember.setCreatedTime(new Date());
        groupMemberDomainService.save(groupMember);

        vo.setAliasName(groupMember.getAliasName());
        vo.setRemark(groupMember.getRemark());
        return vo;
    }

    @Override
    @CacheEvict(value = "#vo.getId()")
    @Transactional(rollbackFor = Exception.class)
    public GroupVO modifyGroup(GroupVO vo) {
        if (vo == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        //更新群组信息
        vo = groupDomainService.modifyGroup(vo, session.getUserId());
        GroupMember groupMember = groupMemberDomainService.getGroupMemberByUserIdAndGroupId(session.getUserId(), vo.getId());
        if (groupMember == null) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "您不是群聊的成员");
        }
        groupMember.setAliasName(StringUtils.isEmpty(vo.getAliasName()) ? session.getNickName() : vo.getAliasName());
        groupMember.setRemark(vo.getRemark());
        if ( groupMemberDomainService.updateGroupMember(groupMember)){
            logger.info("修改群聊，群聊id:{},群聊名称:{}", vo.getId(), vo.getName());
            //TODO 发送异步事件
            IMGroupEvent imGroupEvent = new IMGroupEvent(vo.getId(), session.getUserId(), IMPlatformConstants.GROUP_HANDLER_MODIFY, this.getTopicEvent());
            messageEventSenderService.send(imGroupEvent);
        }
        return vo;
    }

    @Override
    @CacheEvict(value = "#groupId")
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long groupId) {
        if (groupId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        //标记删除群组
        boolean result = groupDomainService.deleteGroup(groupId, session.getUserId());
        //群组标记删除成功
        if (result){
            //删除群成员
            groupMemberDomainService.removeMemberByGroupId(groupId);
            logger.info("删除群聊，群聊id:{}", groupId);
            //TODO 发送异步事件
            IMGroupEvent imGroupEvent = new IMGroupEvent(groupId, session.getUserId(), IMPlatformConstants.GROUP_HANDLER_DELETE, this.getTopicEvent());
            messageEventSenderService.send(imGroupEvent);
        }else{
            logger.info("删除群聊失败");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitGroup(Long groupId) {
        if (groupId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        //验证是否可执行退群操作
        if (!groupDomainService.quitGroup(groupId, session.getUserId())){
            throw new IMException(HttpCode.PROGRAM_ERROR, "不可退出群组");
        }
        if (groupMemberDomainService.removeMember(session.getUserId(), groupId)){
            logger.info("用户退群成功，用户id:{}, 群id:{}", session.getUserId(), groupId);
            //TODO 发送异步事件
            IMGroupEvent imGroupEvent = new IMGroupEvent(groupId, session.getUserId(), IMPlatformConstants.GROUP_HANDLER_QUIT, this.getTopicEvent());
            messageEventSenderService.send(imGroupEvent);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void kickGroup(Long groupId, Long userId) {
        if (groupId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        UserSession session = SessionContext.getSession();
        if (!groupDomainService.kickGroup(groupId, userId, session.getUserId())){
            throw new IMException(HttpCode.PROGRAM_ERROR, "踢人异常");
        }
        if (groupMemberDomainService.removeMember(userId, groupId)){
            logger.info("群主踢人成功，群主id:{}, 群成员id:{}, 群id:{}", session.getUserId(), userId, groupId);
            //TODO 发送异步事件
            IMGroupEvent imGroupEvent = new IMGroupEvent(groupId, session.getUserId(), IMPlatformConstants.GROUP_HANDLER_KICK, this.getTopicEvent());
            messageEventSenderService.send(imGroupEvent);
        }
    }

    @Override
    public List<GroupVO> findGroups() {
        return distributedCacheService.queryWithPassThroughList(IMPlatformConstants.PLATFORM_REDIS_GROUP_LIST_KEY,
                SessionContext.getSession().getUserId(),
                GroupVO.class,
                groupDomainService ::getGroupVOListByUserId,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES);
    }

    @Override
    public void invite(GroupInviteVO vo) {
        if (vo == null || CollectionUtil.isEmpty(vo.getFriendIds())){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        String groupName = groupDomainService.getGroupName(vo.getGroupId());
        if (StrUtil.isEmpty(groupName)){
            throw new IMException(HttpCode.PROGRAM_ERROR, "群聊不存在");
        }
        //获取群组现有群成员
        List<GroupMember> members = groupMemberDomainService.getGroupMemberListByGroupId(vo.getGroupId());
        //去掉已经移除群的
        long size  = CollectionUtil.isEmpty(members) ? 0 : members.size();
        //一个群最多500人
        if (vo.getFriendIds().size() + size > IMConstants.MAX_GROUP_MEMBER){
            throw new IMException(HttpCode.PROGRAM_ERROR, "群聊人数不能大于" + IMConstants.MAX_GROUP_MEMBER + "人");
        }
        UserSession session = SessionContext.getSession();
        //获取好友数据
        List<Friend> friendList = friendDubboService.getFriendByUserId(session.getUserId());
        if (friendList == null){
            friendList = Collections.emptyList();
        }
        List<Friend> finalFriendList = friendList;
        List<Friend> userFriendList = vo.getFriendIds().stream().map(id -> finalFriendList.stream().filter(f -> f.getFriendId().equals(id)).findFirst().get()).collect(Collectors.toList());
        if (finalFriendList.size() != vo.getFriendIds().size()){
            throw new IMException(HttpCode.PROGRAM_ERROR, "部分用户不是您的好友，邀请失败");
        }
        //保存或者更新成功
        if (groupMemberDomainService.saveGroupMemberList(this.getGroupMemberList(vo, groupName, members, userFriendList))){
            logger.info("邀请进入群聊，群聊id:{},群聊名称:{},被邀请用户id:{}", vo.getGroupId(), groupName, vo.getFriendIds());
            //TODO 发送异步事件
            IMGroupEvent imGroupEvent = new IMGroupEvent(vo.getGroupId(), session.getUserId(), IMPlatformConstants.GROUP_HANDLER_INVITE, this.getTopicEvent());
            messageEventSenderService.send(imGroupEvent);
        }
    }

    private List<GroupMember> getGroupMemberList(GroupInviteVO vo, String groupName, List<GroupMember> members, List<Friend> userFriendList) {
        return userFriendList.stream().map(f -> {
            Optional<GroupMember> optional = members.stream().filter(m -> m.getUserId().equals(f.getFriendId())).findFirst();
            GroupMember groupMember = optional.orElseGet(GroupMember::new);
            groupMember.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId());
            groupMember.setGroupId(vo.getGroupId());
            groupMember.setUserId(f.getFriendId());
            groupMember.setAliasName(f.getFriendNickName());
            groupMember.setRemark(groupName);
            groupMember.setHeadImage(f.getFriendHeadImage());
            groupMember.setCreatedTime(new Date());
            groupMember.setQuit(false);
            return groupMember;
        }).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "#groupId")
    public Group getById(Long groupId) {
        if (groupId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return distributedCacheService.queryWithPassThrough(IMPlatformConstants.PLATFORM_REDIS_GROUP_SINGLE_KEY,
                groupId,
                Group.class,
                groupDomainService::getGroupById,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES);
    }

    @Override
    public GroupVO findById(Long groupId) {
        if (groupId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        GroupVO groupVO = distributedCacheService.queryWithPassThrough(IMPlatformConstants.PLATFORM_REDIS_GROUP_VO_SINGLE_KEY,
                            new GroupParams(SessionContext.getSession().getUserId(), groupId),
                            GroupVO.class,
                            groupDomainService :: getGroupVOByParams,
                            IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                            TimeUnit.MINUTES
                        );
        if (groupVO == null){
            throw new IMException(HttpCode.PROGRAM_ERROR, "您未加入群聊");
        }
        return groupVO;
    }

    @Override
    public List<GroupMemberVO> findGroupMembers(Long groupId) {
        if (groupId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return distributedCacheService.queryWithPassThroughList(IMPlatformConstants.PLATFORM_REDIS_MEMBER_VO_LIST_KEY,
                    groupId,
                    GroupMemberVO.class,
                    this::getGroupMemberVOS,
                    IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                    TimeUnit.MINUTES);
    }

    @Override
    public GroupMemberSimpleVO getGroupMemberSimpleVO(GroupParams groupParams) {
        if (groupParams == null || groupParams.isEmpty()){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return distributedCacheService.queryWithPassThrough(IMPlatformConstants.PLATFORM_REDIS_MEMBER_VO_SIMPLE_KEY,
                groupParams,
                GroupMemberSimpleVO.class,
                groupMemberDomainService :: getGroupMemberSimpleVO,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES);
    }

    @Override
    public List<Long> getUserIdsByGroupId(Long groupId) {
        if (groupId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return distributedCacheService.queryWithPassThroughList(IMPlatformConstants.PLATFORM_REDIS_MEMBER_ID_KEY,
                groupId,
                Long.class,
                groupMemberDomainService :: getUserIdsByGroupId,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES);
    }

    @Override
    public List<Long> getGroupIdsByUserId(Long userId) {
        List<GroupMemberSimpleVO> list = this.getGroupMemberSimpleVOList(userId);
        if (CollectionUtil.isEmpty(list)){
            return Collections.emptyList();
        }
        return list.stream().map((GroupMemberSimpleVO::getGroupId)).collect(Collectors.toList());
    }

    @Override
    public List<GroupMemberSimpleVO> getGroupMemberSimpleVOList(Long userId) {
        if (userId == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        return distributedCacheService.queryWithPassThroughList(IMPlatformConstants.PLATFORM_REDIS_MEMBER_LIST_SIMPLE_KEY,
                userId,
                GroupMemberSimpleVO.class,
                groupMemberDomainService :: getGroupMemberSimpleVOList,
                IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME,
                TimeUnit.MINUTES);
    }

    @Override
    public boolean updateHeadImgByUserId(String headImg, Long userId) {
        return groupMemberDomainService.updateHeadImgByUserId(headImg, userId);
    }

    @NotNull
    private List<GroupMemberVO> getGroupMemberVOS(Long groupId) {
        List<GroupMemberVO> memberList = groupMemberDomainService.getGroupMemberVoListByGroupId(groupId);
        List<Long> userList = memberList.stream().map(GroupMemberVO::getUserId).collect(Collectors.toList());
        List<Long> onlineUserIdList = imClient.getOnlineUserList(userList);
//        return memberList.stream().map(m ->{
//            m.setOnline(onlineUserIdList.contains(m.getUserId()));
//            return m;
//        }).sorted((m1, m2) -> m2.getOnline().compareTo(m1.getOnline())).collect(Collectors.toList());
        return memberList.stream().peek(m -> m.setOnline(onlineUserIdList.contains(m.getUserId()))).sorted((m1, m2) -> m2.getOnline().compareTo(m1.getOnline())).collect(Collectors.toList());
    }

    /**
     * 获取主题事件
     */
    private String getTopicEvent(){
        return IMPlatformConstants.EVENT_PUBLISH_TYPE_ROCKETMQ.equals(eventType) ? IMPlatformConstants.TOPIC_EVENT_ROCKETMQ_GROUP : IMPlatformConstants.TOPIC_EVENT_COLA;
    }
}
