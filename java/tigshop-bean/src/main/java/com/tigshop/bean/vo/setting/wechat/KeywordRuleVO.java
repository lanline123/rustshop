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
import java.util.List;

/**
 * 公众号关键字规则列表行 VO
 * <p>
 * 对应接口 GET /adminapi/wechat/keywordRule/list 的列表行。
 * 关键字数量 / 回复数量、关键字文本拼接、按消息类型拆分的回复数
 * 全部由 Mapper 层基于关联表聚合得到，供管理端列表直接渲染。
 *
 * @author Tigshop团队
 * @create 2026-04-23
 */
@Data
@Schema(description = "公众号关键字规则列表行 VO")
public class KeywordRuleVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "已配置关键字数")
    private Integer keywordCount;

    @Schema(description = "已配置回复数")
    private Integer replyCount;

    @Schema(description = "关键字列表（按 sort_order 升序返回，由前端自行拼接展示）")
    private List<String> keywords;

    @Schema(description = "文本类型回复数量（msg_type=1）")
    private Integer textReplyCount;

    @Schema(description = "图文类型回复数量（msg_type=2）")
    private Integer newsReplyCount;

    @Schema(description = "回复模式：1=随机回复 2=全部回复")
    private Integer replyMode;

    @Schema(description = "启用状态：0=停用 1=启用")
    private Integer enabled;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
