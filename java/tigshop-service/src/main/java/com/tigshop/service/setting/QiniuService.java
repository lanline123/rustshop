// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting;

import java.io.InputStream;

/**
 * 七牛云存储服务
 *
 * @author Tigshop团队
 * @create 2025年12月05日
 */
public interface QiniuService {

    /**
     * 上传文件到指定路径
     *
     * @param filePath 文件的本地路径
     * @param objectName 存储对象的名称（通常是文件在存储服务中的路径）
     * @return 上传成功后的文件URL或标识符
     */
    String uploadFile(String filePath, String objectName);

    /**
     * 上传字节数组到指定路径
     *
     * @param content 字节数组内容
     * @param objectName 存储对象的名称（通常是文件在存储服务中的路径）
     * @return 上传成功后的文件URL或标识符
     */
    String uploadBytes(byte[] content, String objectName);

    /**
     * 上传文件流到指定路径
     *
     * @param inputStream 文件的输入流
     * @param objectName 存储对象的名称（通常是文件在存储服务中的路径）
     * @return 上传成功后的文件路径
     */
    String uploadFileStream(String objectName, InputStream inputStream);

    /**
     * 下载指定路径的文件
     *
     * @param objectName 存储对象的名称（通常是文件在存储服务中的路径）
     * @return 文件的输入流
     */
    InputStream downloadFile(String objectName);

    /**
     * 删除指定路径的文件
     *
     * @param objectName 存储对象的名称（通常是文件在存储服务中的路径）
     * @return 是否删除成功
     */
    boolean deleteFile(String objectName);

    /**
     * 生成文件的访问URL
     *
     * @param objectName 存储对象的名称（通常是文件在存储服务中的路径）
     * @return 文件的访问URL
     */
    String generateFileUrl(String objectName);
}

