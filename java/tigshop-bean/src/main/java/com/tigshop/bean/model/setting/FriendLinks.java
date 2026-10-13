// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.setting;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 友情链接model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("friend_links")
@Schema(description = "友情链接")
public class FriendLinks implements Serializable {
    @TableId(value = "link_id", type = IdType.AUTO)
    @Schema(description = "友情链接ID")
    private Integer linkId;

    @Schema(description ="友情链接标题")
    private String linkTitle;

    @Schema(description ="友情链接LOGO")
    private String linkLogo;

    @Schema(description ="链接地址")
    private String linkUrl;

    @Schema(description ="友情链接排序")
    private Integer sortOrder;
}