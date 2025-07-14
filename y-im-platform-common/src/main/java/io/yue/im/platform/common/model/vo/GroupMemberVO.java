package io.yue.im.platform.common.model.vo;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @description 群成员
 */
@ApiModel("群成员信息VO")
public class GroupMemberVO extends GroupMemberSimpleVO{

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("头像")
    private String headImage;

    @ApiModelProperty(value = "是否在线")
    private Boolean online;

    @ApiModelProperty("备注")
    private String remark;

    public GroupMemberVO() {
    }

    public GroupMemberVO(Long userId, String aliasName, String headImage, Boolean quit, Boolean online, String remark) {
        super(aliasName, quit);
        this.userId = userId;
        this.headImage = headImage;
        this.online = online;
        this.remark = remark;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
