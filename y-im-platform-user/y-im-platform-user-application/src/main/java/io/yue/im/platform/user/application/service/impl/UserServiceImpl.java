package io.yue.im.platform.user.application.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import io.yue.im.common.cache.distribute.DistributedCacheService;
import io.yue.im.common.cache.id.SnowFlakeFactory;
import io.yue.im.common.domain.enums.IMTerminalType;
import io.yue.im.common.domain.jwt.JwtUtils;
import io.yue.im.common.mq.MessageSenderService;
import io.yue.im.platform.common.exception.IMException;
import io.yue.im.platform.common.jwt.JwtProperties;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.dto.LoginDTO;
import io.yue.im.platform.common.model.dto.ModifyPwdDTO;
import io.yue.im.platform.common.model.dto.RegisterDTO;
import io.yue.im.platform.common.model.entity.User;
import io.yue.im.platform.common.model.enums.HttpCode;
import io.yue.im.platform.common.model.event.User2FriendEvent;
import io.yue.im.platform.common.model.event.User2GroupEvent;
import io.yue.im.platform.common.model.vo.LoginVO;
import io.yue.im.platform.common.model.vo.OnlineTerminalVO;
import io.yue.im.platform.common.model.vo.UserVO;
import io.yue.im.platform.common.session.SessionContext;
import io.yue.im.platform.common.session.UserSession;
import io.yue.im.platform.common.utils.BeanUtils;
import io.yue.im.platform.user.application.service.UserService;
import io.yue.im.platform.user.domain.service.UserDomainService;
import io.yue.im.sdk.client.IMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserDomainService userDomainService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DistributedCacheService distributedCacheService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private IMClient imClient;
    @Autowired
    private MessageSenderService messageSenderService;

    @Override
    public LoginVO login(LoginDTO dto) {
        if (dto == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        User user = distributedCacheService.queryWithPassThrough(IMPlatformConstants.PLATFORM_REDIS_USER_KEY, dto.getUserName(), User.class,  userDomainService::getUserByUserName, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        if (user == null){
            throw new IMException(HttpCode.PROGRAM_ERROR, "当前用户不存在");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())){
            throw new IMException(HttpCode.PASSWOR_ERROR);
        }
        //生成token
        UserSession session = BeanUtils.copyProperties(user, UserSession.class);
        session.setUserId(user.getId());
        session.setTerminal(dto.getTerminal());
        String strJson = JSON.toJSONString(session);
        String accessToken = JwtUtils.sign(user.getId(), strJson, jwtProperties.getAccessTokenExpireIn(), jwtProperties.getAccessTokenSecret());
        String refreshToken = JwtUtils.sign(user.getId(), strJson, jwtProperties.getRefreshTokenExpireIn(), jwtProperties.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        return vo;
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        //验证 token
        if (!JwtUtils.checkSign(refreshToken, jwtProperties.getRefreshTokenSecret())) {
            throw new IMException("refreshToken无效或已过期");
        }
        String strJson = JwtUtils.getInfo(refreshToken);
        Long userId = JwtUtils.getUserId(refreshToken);
        String accessToken = JwtUtils.sign(userId, strJson, jwtProperties.getAccessTokenExpireIn(), jwtProperties.getAccessTokenSecret());
        String newRefreshToken = JwtUtils.sign(userId, strJson, jwtProperties.getRefreshTokenExpireIn(), jwtProperties.getRefreshTokenSecret());
        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpiresIn(jwtProperties.getAccessTokenExpireIn());
        vo.setRefreshToken(newRefreshToken);
        vo.setRefreshTokenExpiresIn(jwtProperties.getRefreshTokenExpireIn());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyPassword(ModifyPwdDTO dto) {
        //获取用户Session
        UserSession session = SessionContext.getSession();
        //不从缓存中获取，防止缓存数据不一致
        User user = userDomainService.getById(session.getUserId());
        if (user == null){
            throw new IMException("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IMException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userDomainService.saveOrUpdateUser(user);
        logger.info("用户修改密码，用户id:{},用户名:{},昵称:{}", user.getId(), user.getUserName(), user.getNickName());
    }

    @Override
    public User findUserByUserName(String username) {
        User user = distributedCacheService.queryWithPassThrough(IMPlatformConstants.PLATFORM_REDIS_USER_KEY, username, User.class,  userDomainService::getUserByUserName, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        if (user == null){
            throw new IMException(HttpCode.PROGRAM_ERROR, "当前用户不存在");
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserVO vo) {
        UserSession session = SessionContext.getSession();
        if (!session.getUserId().equals(vo.getId())) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "不允许修改其他用户的信息!");
        }
        User user = userDomainService.getById(vo.getId());
        if (Objects.isNull(user)) {
            throw new IMException(HttpCode.PROGRAM_ERROR, "用户不存在");
        }
        //更新用户的基本信息
        if (!StrUtil.isEmpty(vo.getNickName())){
            user.setNickName(vo.getNickName());
        }
        if (vo.getSex() != null){
            user.setSex(vo.getSex());
        }
        if (!StrUtil.isEmpty(vo.getSignature())){
            user.setSignature(vo.getSignature());
        }
        if (!StrUtil.isEmpty(vo.getHeadImage())){
            user.setHeadImage(vo.getHeadImage());
        }
        if (!StrUtil.isEmpty(vo.getHeadImageThumb())){
            user.setHeadImageThumb(vo.getHeadImageThumb());
        }
        boolean result = userDomainService.saveOrUpdateUser(user);
        if (!result) return;
        //TODO 如果用户更新了昵称和头像，则更新好友昵称和头像
        if (!user.getNickName().equals(vo.getNickName()) || !user.getHeadImageThumb().equals(vo.getHeadImageThumb())) {
            User2FriendEvent user2FriendEvent = new User2FriendEvent(session.getUserId(), vo.getNickName(), vo.getHeadImageThumb(), IMPlatformConstants.TOPIC_USER_TO_FRIEND);
            messageSenderService.send(user2FriendEvent);
        }
        //TODO 群聊中的头像是缩略图，需要更新群聊中的头像
        if (!user.getNickName().equals(vo.getNickName()) || !user.getHeadImageThumb().equals(vo.getHeadImageThumb())) {
            User2GroupEvent user2GroupEvent = new User2GroupEvent(session.getUserId(), vo.getHeadImageThumb(), IMPlatformConstants.TOPIC_USER_TO_GROUP);
            messageSenderService.send(user2GroupEvent);
        }
    }

    @Override
    public UserVO findUserById(Long id, boolean constantsOnlineFlag) {
        User user = this.getUserById(id);
        if (user == null){
            throw new IMException(HttpCode.PROGRAM_ERROR, "当前用户不存在");
        }
        UserVO vo = BeanUtils.copyProperties(user, UserVO.class);
        if (constantsOnlineFlag){
            vo.setOnline(imClient.isOnline(id));
        }
        return vo;
    }

    @Override
    public User getUserById(Long userId) {
        return distributedCacheService.queryWithPassThrough(IMPlatformConstants.PLATFORM_REDIS_USER_KEY, userId, User.class,  userDomainService::getById, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
    }

    @Override
    public List<UserVO> findUserByName(String name) {
        List<User> userList = userDomainService.findUserByName(name);
        //TODO 调用IMClient的方法后处理在线状态
        if (CollectionUtil.isEmpty(userList)){
            return Collections.emptyList();
        }
        List<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toList());
        List<Long> onlineUserIds = imClient.getOnlineUserList(userIds);
        return userList.stream().map(u -> {
            UserVO vo = BeanUtils.copyProperties(u, UserVO.class);
            vo.setOnline(onlineUserIds.contains(u.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<OnlineTerminalVO> getOnlineTerminals(String userIds) {
        List<Long> userIdList = Arrays.stream(userIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        // 查询在线的终端
        Map<Long, List<IMTerminalType>> terminalMap = imClient.getOnlineTerminal(userIdList);
        // 组装vo
        List<OnlineTerminalVO> vos = new LinkedList<>();
        terminalMap.forEach((userId, types) -> {
            List<Integer> terminals = types.stream().map(IMTerminalType::code).collect(Collectors.toList());
            vos.add(new OnlineTerminalVO(userId, terminals));
        });
        return vos;
    }

    @Override
    public void register(RegisterDTO dto) {
        if (dto == null){
            throw new IMException(HttpCode.PARAMS_ERROR);
        }
        User user = distributedCacheService.queryWithPassThrough(IMPlatformConstants.PLATFORM_REDIS_USER_KEY, dto.getUserName(), User.class,  userDomainService::getUserByUserName, IMPlatformConstants.DEFAULT_REDIS_CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        if (user != null){
            throw new IMException(HttpCode.USERNAME_ALREADY_REGISTER);
        }
        user = BeanUtils.copyProperties(dto, User.class);
        user.setId(SnowFlakeFactory.getSnowFlakeFromCache().nextId());
        user.setCreatedTime(new Date());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDomainService.saveOrUpdateUser(user);
        logger.info("注册用户，用户id:{},用户名:{},昵称:{}", user.getId(), dto.getUserName(), dto.getNickName());
    }
}
