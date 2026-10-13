// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

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

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.comm.ResponseMessage;

import java.io.InputStream;

/**
 * 阿里云oss存储服务
 *
 * @author Jayce
 * @create 2024年11月14日 10:22
 */
public interface OssService {

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
     * @return 操作结果的响应消息
     */
    ResponseMessage deleteFile(String objectName);

    /**
     * 生成文件的访问URL（使用指定的OSS客户端）
     *
     * @param objectName 存储对象的名称（通常是文件在存储服务中的路径）
     * @param ossClient 指定的OSS客户端
     * @return 文件的访问URL
     */
    String generateFileUrl(String objectName, OSS ossClient);

    /**
     * 上传文件到指定路径
     * @param inputStream 文件的输入流
     * @param filePath 路径
     * @return String url
     */
    String uploadFile(InputStream inputStream, String filePath);
}
