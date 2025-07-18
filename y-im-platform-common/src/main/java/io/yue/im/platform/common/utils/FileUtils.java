package io.yue.im.platform.common.utils;

/**
 * @description 文件工具类
 */
public class FileUtils {

    /**
     * 获取文件后缀
     *
     * @param fileName  文件名
     * @return boolean
     */
    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 判断文件是否图片类型
     *
     * @param fileName  文件名
     * @return  boolean
     */
    public static boolean isImage(String fileName) {
        String extension = getFileExtension(fileName);
        String[] imageExtension = new String[]{"jpeg", "jpg", "bmp", "png","webp","gif"};
        for (String e : imageExtension){
            if (extension.toLowerCase().equals(e)) {
                return true;
            }
        }
        return false;
    }
}
