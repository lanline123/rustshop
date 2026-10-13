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

import java.io.File;
import java.io.InputStream;

/**
 * AWS S3 存储服务接口
 *
 * @author Tigshop团队
 * @create 2026年01月19日
 */
public interface AwsS3Service {

    /**
     * 上传文件流到 S3
     *
     * @param objectName  存储对象的名称（文件在 S3 中的路径）
     * @param inputStream 文件输入流
     * @return 文件路径
     */
    String uploadFileStream(String objectName, InputStream inputStream);

    /**
     * 上传文件到 S3
     *
     * @param objectName 存储对象的名称（文件在 S3 中的路径）
     * @param file       文件对象
     * @return 文件路径
     */
    String uploadFile(String objectName, File file);

    /**
     * 下载文件
     *
     * @param objectName 存储对象的名称
     * @return 文件输入流
     */
    InputStream downloadFile(String objectName);

    /**
     * 删除文件
     *
     * @param objectName 存储对象的名称
     * @return 是否删除成功
     */
    boolean deleteFile(String objectName);
}
