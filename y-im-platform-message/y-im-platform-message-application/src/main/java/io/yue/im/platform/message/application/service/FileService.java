package io.yue.im.platform.message.application.service;

import io.yue.im.platform.common.model.enums.FileType;
import io.yue.im.platform.common.model.vo.UploadImageVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description 文件服务
 */
public interface FileService {

    /**
     * 上传文件
     */
    String uploadFile(MultipartFile file);

    /**
     * 上传图片
     */
    UploadImageVO uploadImage(MultipartFile file);

    /**
     * 生成文件URL
     */
    String getFileUrl(FileType fileTypeEnum, String fileName);
}
