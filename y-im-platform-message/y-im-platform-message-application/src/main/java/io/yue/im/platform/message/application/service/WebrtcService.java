package io.yue.im.platform.message.application.service;


import io.yue.im.platform.message.application.ice.ICEServer;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description WebRTC
 */
public interface WebrtcService {
    /**
     * 视频呼叫
     */
    void call(Long uid, String offer);

    /**
     * 取消呼叫
     */
    void cancel(Long uid);

    /**
     * 呼叫失败
     */
    void failed( Long uid, String reason);

    /**
     * 接受视频呼叫
     */
    void accept(Long uid, @RequestBody String answer);

    /**
     * 拒绝视频呼叫
     */
    void reject(Long uid);

    /**
     * 挂断视频通话
     */
    void leave(Long uid);

    /**
     * 同步
     */
    void candidate( Long uid, String candidate);

    /**
     * 获取ICE服务列表
     */
    List<ICEServer> getIceServers();
}
