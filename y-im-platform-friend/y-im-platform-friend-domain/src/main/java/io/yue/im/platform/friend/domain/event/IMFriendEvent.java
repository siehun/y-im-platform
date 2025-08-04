package io.yue.im.platform.friend.domain.event;


import io.yue.im.common.domain.event.IMBaseEvent;

/**
 * @description 好友事件模型
 */
public class IMFriendEvent extends IMBaseEvent {
    //操作
    private String handler;

    //好友id
    private Long friendId;

    public IMFriendEvent() {
    }

    public IMFriendEvent(Long id, Long friendId, String handler, String destination) {
        super(id, destination);
        this.handler = handler;
        this.friendId = friendId;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}
