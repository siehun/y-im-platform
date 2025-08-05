package io.yue.im.platform.message.application.ice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @description ICE服务配置
 */
@Component
@ConfigurationProperties(prefix="webrtc")
public class ICEServerConfig {

    private List<ICEServer> iceServers = new ArrayList<>();

    public List<ICEServer> getIceServers() {
        return iceServers;
    }

    public void setIceServers(List<ICEServer> iceServers) {
        this.iceServers = iceServers;
    }
}
