package io.yue.im.platform.common.session;


import io.yue.im.common.domain.model.IMSessionInfo;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description 用户Session信息
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
public class UserSession extends IMSessionInfo {

    /*
     * 用户名称
     */
    private String userName;

    /*
     * 用户昵称
     */
    private String nickName;

    public UserSession() {
    }

    public UserSession(Long userId, Integer terminal, String userName, String nickName) {
        super(userId, terminal);
        this.userName = userName;
        this.nickName = nickName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
