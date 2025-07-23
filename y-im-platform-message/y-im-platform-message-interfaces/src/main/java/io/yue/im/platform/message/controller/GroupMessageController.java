package io.yue.im.platform.message.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.yue.im.platform.common.model.dto.GroupMessageDTO;
import io.yue.im.platform.common.model.vo.GroupMessageVO;
import io.yue.im.platform.common.response.ResponseMessage;
import io.yue.im.platform.common.response.ResponseMessageFactory;
import io.yue.im.platform.message.application.service.GroupMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description 群聊消息
 */
@Api(tags = "群聊消息")
@RestController
@RequestMapping("/message/group")
public class GroupMessageController {

    @Autowired
    private GroupMessageService groupMessageService;

    @PostMapping("/send")
    @ApiOperation(value = "发送群聊消息",notes="发送群聊消息")
    public ResponseMessage<Long> sendMessage(@Valid @RequestBody GroupMessageDTO dto){
        return ResponseMessageFactory.getSuccessResponseMessage(groupMessageService.sendMessage(dto));
    }

    @PostMapping("/pullUnreadMessage")
    @ApiOperation(value = "拉取未读消息",notes="拉取未读消息")
    public ResponseMessage pullUnreadMessage(){
        groupMessageService.pullUnreadMessage();
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @GetMapping("/loadMessage")
    @ApiOperation(value = "拉取消息",notes="拉取消息,一次最多拉取100条")
    public ResponseMessage<List<GroupMessageVO>> loadMessage(@RequestParam Long minId){
        return ResponseMessageFactory.getSuccessResponseMessage(groupMessageService.loadMessage(minId));
    }

    @GetMapping("/history")
    @ApiOperation(value = "查询聊天记录",notes="查询聊天记录")
    public ResponseMessage<List<GroupMessageVO>> recallMessage(@NotNull(message = "群聊id不能为空") @RequestParam Long groupId,
                                                               @NotNull(message = "页码不能为空") @RequestParam Long page,
                                                               @NotNull(message = "size不能为空") @RequestParam Long size){
        return ResponseMessageFactory.getSuccessResponseMessage( groupMessageService.findHistoryMessage(groupId,page,size));
    }

    @PutMapping("/readed")
    @ApiOperation(value = "消息已读",notes="将群聊中的消息状态置为已读")
    public ResponseMessage readedMessage(@RequestParam Long groupId){
        groupMessageService.readedMessage(groupId);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @DeleteMapping("/recall/{id}")
    @ApiOperation(value = "撤回消息",notes="撤回群聊消息")
    public ResponseMessage<Long> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id){
        groupMessageService.recallMessage(id);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }
}
