package io.yue.im.platform.message.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.yue.im.platform.common.model.enums.HttpCode;
import io.yue.im.platform.common.model.vo.UploadImageVO;
import io.yue.im.platform.common.response.ResponseMessage;
import io.yue.im.platform.common.response.ResponseMessageFactory;
import io.yue.im.platform.message.application.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description 文件接口
 */
@RestController
@Api(tags = "文件上传")
public class FileController {

    @Autowired
    private FileService fileService;

    @ApiOperation(value = "上传图片",notes="上传图片,上传后返回原图和缩略图的url")
    @PostMapping("/image/upload")
    public ResponseMessage<UploadImageVO> uploadImage(MultipartFile file) {
        return ResponseMessageFactory.getSuccessResponseMessage(fileService.uploadImage(file));
    }

    @ApiOperation(value = "上传文件",notes="上传文件，上传后返回文件url")
    @PostMapping("/file/upload")
    public ResponseMessage<String> uploadFile(MultipartFile file) {
        return ResponseMessageFactory.getSuccessResponseMessage(fileService.uploadFile(file), HttpCode.SUCCESS.getMsg());
    }
}
