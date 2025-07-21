package io.yue.im.platform.message.application.service.impl;

import cn.hutool.core.util.StrUtil;
import io.yue.im.common.domain.constants.IMConstants;
import io.yue.im.platform.common.exception.IMException;
import io.yue.im.platform.common.model.constants.IMPlatformConstants;
import io.yue.im.platform.common.model.enums.FileType;
import io.yue.im.platform.common.model.enums.HttpCode;
import io.yue.im.platform.common.model.vo.UploadImageVO;
import io.yue.im.platform.common.session.SessionContext;
import io.yue.im.platform.common.utils.FileUtils;
import io.yue.im.platform.common.utils.ImageUtils;
import io.yue.im.platform.message.application.minio.MinioService;
import io.yue.im.platform.message.application.service.FileService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * @description 文件服务
 */
@Service
public class FileServiceImpl implements FileService {
    private final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final MinioService minioService;

    @Value("${minio.public}")
    private String minIoServer;
    @Value("${minio.bucketName}")
    private String bucketName;
    @Value("${minio.imagePath}")
    private String imagePath;
    @Value("${minio.filePath}")
    private String filePath;

    public FileServiceImpl(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostConstruct
    public void init(){
        if (!minioService.bucketExists(bucketName)){
            minioService.makeBucket(bucketName);
            minioService.setBucketPublic(bucketName);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        Long userId = SessionContext.getSession().getUserId();
        // 大小校验
        if(file.getSize() > IMConstants.MAX_FILE_SIZE){
            throw new IMException(HttpCode.PROGRAM_ERROR, "文件大小不能超过10M");
        }
        // 上传
        String fileName = minioService.upload(bucketName, filePath, file);
        if(StringUtils.isEmpty(fileName)){
            throw new IMException(HttpCode.PROGRAM_ERROR, "文件上传失败");
        }
        String url =  getFileUrl(FileType.FILE, fileName);
        logger.info("文件文件成功，用户id:{}, url:{}", userId, url);
        return url;
    }

    @Override
    public UploadImageVO uploadImage(MultipartFile file) {
        try {
            Long userId = SessionContext.getSession().getUserId();
            // 大小校验
            if(file.getSize() > IMConstants.MAX_IMAGE_SIZE){
                throw new IMException(HttpCode.PROGRAM_ERROR, "图片大小不能超过5M");
            }
            // 图片格式校验
            if(!FileUtils.isImage(file.getOriginalFilename())){
                throw new IMException(HttpCode.PROGRAM_ERROR, "图片格式不合法");
            }
            // 上传原图
            UploadImageVO vo = new UploadImageVO();
            String fileName = minioService.upload(bucketName, imagePath, file);
            if(StringUtils.isEmpty(fileName)){
                throw new IMException(HttpCode.PROGRAM_ERROR, "图片上传失败");
            }
            vo.setOriginUrl(getFileUrl(FileType.IMAGE, fileName));
            // 大于30K的文件需上传缩略图
            if(file.getSize() > IMPlatformConstants.IMAGE_COMPRESS_LIMIT){
                byte[] imageByte = ImageUtils.compressForScale(file.getBytes(), IMPlatformConstants.IMAGE_COMPRESS_SIZE);
                fileName = minioService.upload(bucketName, imagePath, file.getOriginalFilename(), imageByte, file.getContentType());
                if(StringUtils.isEmpty(fileName)){
                    throw new IMException(HttpCode.PROGRAM_ERROR, "图片上传失败");
                }
            }
            vo.setThumbUrl(getFileUrl(FileType.IMAGE, fileName));
            logger.info("文件图片成功，用户id:{}, url:{}",userId, vo.getOriginUrl());
            return vo;
        } catch (IOException e) {
            logger.error("上传图片失败，{}", e.getMessage(), e);
            throw new IMException(HttpCode.PROGRAM_ERROR, "图片上传失败");
        }
    }

    @Override
    public String getFileUrl(FileType fileTypeEnum, String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append(minIoServer)
                .append("/")
                .append(bucketName)
                .append(StrUtil.isEmpty(fileTypeEnum.getPath()) ? "" : fileTypeEnum.getPath())
                .append(fileName);
        return sb.toString();
    }
}
