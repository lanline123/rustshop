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
 * 友情链接VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "友情链接参数")
public class FriendLinksVO {
    @Schema(description = "友情链接ID")
    private Integer linkId;

    @Schema(description ="友情链接名称")
    private String linkLogo;

    @Schema(description ="友情链接图片")
    private String linkTitle;

    @Schema(description ="友情链接排序")
    private String linkUrl;

    @Schema(description ="排序")
    private Integer sortOrder;

}