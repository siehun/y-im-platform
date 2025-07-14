package io.yue.im.platform.common.model.vo;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @description 在线终端
 */
@ApiModel("在线终端VO")
public class OnlineTerminalVO {

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "在线终端类型")
    private List<Integer> terminals;

    public OnlineTerminalVO() {
    }

    public OnlineTerminalVO(Long userId, List<Integer> terminals) {
        this.userId = userId;
        this.terminals = terminals;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Integer> getTerminals() {
        return terminals;
    }

    public void setTerminals(List<Integer> terminals) {
        this.terminals = terminals;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
