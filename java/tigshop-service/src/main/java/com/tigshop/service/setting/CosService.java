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
 * 腾讯cos接口
 *
 * @author Tigshop团队
 * @create 2025年01月13日 13:41
 */
public interface CosService {
    /**
     * 上传文件
     *
     * @param objectName cos文件路径
     * @param inputStream 文件
     * @return 文件路径
     */
    String uploadFileStream(String objectName, InputStream inputStream);

    boolean downloadFile(String objectName, File destFile);

    boolean deleteFile(String objectName);
}
