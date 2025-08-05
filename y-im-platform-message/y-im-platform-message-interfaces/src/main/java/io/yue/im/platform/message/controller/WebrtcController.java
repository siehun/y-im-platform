package io.yue.im.platform.message.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.yue.im.platform.common.response.ResponseMessage;
import io.yue.im.platform.common.response.ResponseMessageFactory;
import io.yue.im.platform.message.application.ice.ICEServer;
import io.yue.im.platform.message.application.service.WebrtcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description Web RTC
 */
@Api(tags = "webrtc视频通话")
@RestController
@RequestMapping("/webrtc/private")
public class WebrtcController {

    @Autowired
    private WebrtcService webrtcService;

    @ApiOperation(httpMethod = "POST", value = "呼叫视频通话")
    @PostMapping("/call")
    public ResponseMessage<String> call(@RequestParam Long uid, @RequestBody String offer) {
        webrtcService.call(uid,offer);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @ApiOperation(httpMethod = "POST", value = "取消呼叫")
    @PostMapping("/cancel")
    public ResponseMessage<String> cancel(@RequestParam Long uid) {
        webrtcService.cancel(uid);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @ApiOperation(httpMethod = "POST", value = "呼叫失败")
    @PostMapping("/failed")
    public ResponseMessage<String> failed(@RequestParam Long uid,@RequestParam String reason) {
        webrtcService.failed(uid,reason);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @ApiOperation(httpMethod = "POST", value = "接受视频通话")
    @PostMapping("/accept")
    public ResponseMessage<String> accept(@RequestParam Long uid,@RequestBody String answer) {
        webrtcService.accept(uid,answer);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @ApiOperation(httpMethod = "POST", value = "拒绝视频通话")
    @PostMapping("/reject")
    public ResponseMessage<String> reject(@RequestParam Long uid) {
        webrtcService.reject(uid);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @ApiOperation(httpMethod = "POST", value = "挂断")
    @PostMapping("/handup")
    public ResponseMessage<String> leave(@RequestParam Long uid) {
        webrtcService.leave(uid);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @PostMapping("/candidate")
    @ApiOperation(httpMethod = "POST", value = "同步candidate")
    public ResponseMessage<String> forwardCandidate(@RequestParam Long uid,@RequestBody String candidate ) {
        webrtcService.candidate(uid,candidate);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @GetMapping("/iceservers")
    @ApiOperation(httpMethod = "GET", value = "获取iceservers")
    public ResponseMessage<List<ICEServer>>  iceservers() {
        return ResponseMessageFactory.getSuccessResponseMessage(webrtcService.getIceServers());
    }
}
