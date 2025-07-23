package io.yue.im.platform.message.application.minio;

import org.springframework.web.multipart.MultipartFile;

/**
 * @description Monio文件上传
 */
public interface MinioService {

    /**
     * 查看存储bucket是否存在
     */
    Boolean bucketExists(String bucketName);

    /**
     * 创建存储bucket
     */
    void makeBucket(String bucketName);

    /**
     * 设置bucket权限为public
     */
    void setBucketPublic(String bucketName);

    /**
     * 文件上传
     * @param bucketName bucket名称
     * @param path 路径
     * @param file 文件
     * @return Boolean
     */
    String upload(String bucketName, String path, MultipartFile file);

    /**
     * 文件上传
     * @param bucketName bucket名称
     * @param path 路径
     * @param name 文件名
     * @param fileByte 文件内容
     * @param contentType  contentType
     * @return  objectName
     */
    String upload(String bucketName,String path,String name,byte[] fileByte,String contentType);

    /**
     * 删除
     * @param bucketName bucket名称
     * @param path  路径
     * @param fileName 文件名
     * @return true/false
     */
    boolean remove(String bucketName,String path,String fileName);
}
