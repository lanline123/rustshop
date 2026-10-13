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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微信菜单保存并发布结果VO
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Data
@Schema(description = "微信菜单保存并发布结果")
public class SaveAndPublishVO {

    @Schema(description = "微信推送是否成功；true 时本次提交的新菜单 JSON 已同步落库，false 时保留 DB 原菜单不变（与微信侧一致）")
    private Boolean published;

    @Schema(description = "published=false 时的拼接文案：发布失败（{errcode}）：{errmsg}；成功时为 null")
    private String errMsg;

    @Schema(description = "本次推送成功的时间；失败为 null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;
}
