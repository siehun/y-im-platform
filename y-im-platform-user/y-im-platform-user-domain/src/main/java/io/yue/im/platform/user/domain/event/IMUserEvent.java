package io.yue.im.platform.user.domain.event;

import io.yue.im.common.domain.event.IMBaseEvent;
import lombok.Data;

@Data
public class IMUserEvent extends IMBaseEvent {
    private String userName;
    public IMUserEvent(Long id, String userName, String destination) {
        super(id, destination);
        this.userName = userName;
    }

}
