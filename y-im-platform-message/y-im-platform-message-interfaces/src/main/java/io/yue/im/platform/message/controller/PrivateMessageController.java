package io.yue.im.platform.message.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.yue.im.platform.common.model.dto.PrivateMessageDTO;
import io.yue.im.platform.common.model.vo.PrivateMessageVO;
import io.yue.im.platform.common.response.ResponseMessage;
import io.yue.im.platform.common.response.ResponseMessageFactory;
import io.yue.im.platform.message.application.service.PrivateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description 私聊消息
 */
@Api(tags = "私聊消息")
@RestController
@RequestMapping("/message/private")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;

    @PostMapping("/send")
    @ApiOperation(value = "发送消息",notes="发送私聊消息")
    public ResponseMessage<Long> sendMessage(@Valid @RequestBody PrivateMessageDTO dto){
        return ResponseMessageFactory.getSuccessResponseMessage(privateMessageService.sendMessage(dto));
    }

    @PostMapping("/pullUnreadMessage")
    @ApiOperation(value = "拉取未读消息",notes="拉取未读消息")
    public ResponseMessage<String> pullUnreadMessage(){
        privateMessageService.pullUnreadMessage();
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @GetMapping("/loadMessage")
    @ApiOperation(value = "拉取消息",notes="拉取消息,一次最多拉取100条")
    public ResponseMessage<List<PrivateMessageVO>> loadMessage(@RequestParam Long minId){
        return ResponseMessageFactory.getSuccessResponseMessage(privateMessageService.loadMessage(minId));
    }

    @GetMapping("/history")
    @ApiOperation(value = "查询聊天记录",notes="查询聊天记录")
    public ResponseMessage<List<PrivateMessageVO>> recallMessage(@NotNull(message = "好友id不能为空") @RequestParam Long friendId,
                                                                 @NotNull(message = "页码不能为空") @RequestParam Long page,
                                                                 @NotNull(message = "size不能为空") @RequestParam Long size){
        return ResponseMessageFactory.getSuccessResponseMessage(privateMessageService.getHistoryMessage(friendId, page, size));
    }

    @PutMapping("/readed")
    @ApiOperation(value = "消息已读",notes="将会话中接收的消息状态置为已读")
    public ResponseMessage<String> readedMessage(@RequestParam Long friendId){
        privateMessageService.readedMessage(friendId);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @DeleteMapping("/recall/{id}")
    @ApiOperation(value = "撤回消息",notes="撤回私聊消息")
    public ResponseMessage<Long> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id){
        privateMessageService.recallMessage(id);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @GetMapping("/maxReadedId")
    @ApiOperation(value = "获取最大已读消息的id",notes="获取某个会话中已读消息的最大id")
    public ResponseMessage<Long> getMaxReadedId(@RequestParam Long friendId){
        return ResponseMessageFactory.getSuccessResponseMessage(privateMessageService.getMaxReadedId(friendId));
    }
}
