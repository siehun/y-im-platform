package io.yue.im.platform.common.model.vo;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @description 上传图片
 */
@ApiModel("图片上传VO")
public class UploadImageVO {

    @ApiModelProperty(value = "原图")
    private String originUrl;

    @ApiModelProperty(value = "缩略图")
    private String thumbUrl;

    public UploadImageVO() {
    }

    public UploadImageVO(String originUrl, String thumbUrl) {
        this.originUrl = originUrl;
        this.thumbUrl = thumbUrl;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
