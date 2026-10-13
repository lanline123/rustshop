// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.lang;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 语言地区维护表
 *
 * @author Tigshop团队
 * @create 2024年12月30日 11:23
 */
@Data
@TableName("locales")
@Schema(description = "语言地区维护表")
public class Locales {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "语言地区代码，如 'en_US'")
    private String localeCode;

    @Schema(description = "语言，如 'English、中文、繁體中文'")
    private String language;

    @Schema(description = "该语言对应的访问域名，如 'en.domain.com'，不含协议与端口；为空表示不启用域名跳转")
    private String domain;

    @Schema(description = "国旗图片")
    private String flagPicture;

    @Schema(description = "最后更新时间")
    private Long lastUpdated;

    @Schema(description = "是否启用1启用0禁用")
    private Integer isEnabled;

    @Schema(description = "是否默认：1 是 0 否")
    private Integer isDefault;

    @Schema(description = "货币id")
    private Integer currencyId;

    @Schema(description = "排序")
    private Integer sort;
}
