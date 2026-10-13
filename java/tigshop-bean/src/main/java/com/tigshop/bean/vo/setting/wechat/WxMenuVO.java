// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.setting.wechat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tigshop.bean.dto.setting.wechat.WxMenuSaveDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微信菜单返回VO
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Data
@Schema(description = "微信菜单返回结果")
public class WxMenuVO {

    @Schema(description = "菜单记录ID；从未保存过时为 null")
    private Long id;

    @Schema(description = "菜单树；从未保存时返回 { buttons: [] }")
    private WxMenuSaveDTO.WxMenuTree tree;

    @Schema(description = "最近一次发布时间，可空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    @Schema(description = "最近一次保存时间，可空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
