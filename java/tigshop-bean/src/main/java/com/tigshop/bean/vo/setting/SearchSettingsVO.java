// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 搜索设置VO
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
@Data
public class SearchSettingsVO {

    @Schema(description = "搜索类型：es-Elasticsearch，mysql-MySQL")
    private String selectSearchType = "mysql";

    @Schema(description = "ES连接状态")
    private Boolean esConnected = false;

    @Schema(description = "ES版本")
    private String esVersion;

    @Schema(description = "ES集群名称")
    private String esClusterName;
}
