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
 * 公众号关键字规则详情 VO
 * <p>
 * 对应接口 GET /adminapi/wechat/keywordRule/detail/{id}。
 * {@code keywords} 与 {@code replies} 均已按 sortOrder 升序返回。
 *
 * @author Tigshop团队
 * @create 2026-04-23
 */
@Data
@Schema(description = "公众号关键字规则详情 VO")
public class KeywordRuleDetailVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "回复模式：1=随机回复 2=全部回复")
    private Integer replyMode;

    @Schema(description = "启用状态：0=停用 1=启用")
    private Integer enabled;

    @Schema(description = "关键字列表（按 sortOrder 升序）")
    private List<KeywordRuleKeywordItem> keywords;

    @Schema(description = "回复内容列表（按 sortOrder 升序）")
    private List<ReplyPayloadVO> replies;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 关键字条目（嵌入在规则详情中，不含排序字段）
     */
    @Data
    @Schema(description = "关键字条目")
    public static class KeywordRuleKeywordItem {

        @Schema(description = "关键字文本（≤30 字符）")
        private String keyword;

        @Schema(description = "匹配方式：1=完全匹配 2=包含匹配")
        private Integer matchType;
    }
}
