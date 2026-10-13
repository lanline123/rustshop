// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.setting.FriendLinksConstants.FRIEND_LINKS_ID_NOT_NULL;

/**
  * 友情链接更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "友情链接参数")
public class FriendLinksUpdateDTO {
    @Schema(description = "友情链接ID")
    private Integer id;

    @Schema(description = "友情链接ID")
    private Integer linkId;

    @Schema(description ="友情链接标题")
    private String linkTitle;

    @Schema(description ="友情链接LOGO")
    private String linkLogo;

    @Schema(description ="友情链接排序")
    private Integer sortOrder;

    @Schema(description ="链接地址")
    private String linkUrl;
}
