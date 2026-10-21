// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.decorate;

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 页面管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "页面管理参数")
public class DecorateVO {
    @Schema(description = "页面管理ID")
    private Integer decorateId;

    @Schema(description ="模板名称")
    private String decorateTitle;

    @Schema(description ="已发布的页面模块数据")
    private JSONObject data;

    @Schema(description ="草稿页面模块数据")
    private JSONObject draftData;

    @Schema(description ="页面类型 1:h5 2:PC")
    private Integer decorateType;

    @Schema(description ="页面类型 1:商城 2:店铺 3:门店 4:自定义 7:导航")
    private Integer pageType;

    @Schema(description ="是否是首页")
    private Integer isHome;

    @Schema(description ="所属店铺")
    private Integer shopId;

    @Schema(description ="状态 1:发布 0:未发布")
    private Integer status;

    @Schema(description ="更新时间")
    private String updateTime;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "绑定的H5页面ID")
    private Integer bindedH5Id;

    @Schema(description = "绑定的H5页面名称")
    private String bingdingH5Name;

    @Schema(description = "模板样式版本 new新版/old旧版（取自 data.pageModule.homeStyleVersion，缺失兜底 old）")
    private String homeStyleVersion;

    @Schema(description = "子级")
    private List<ChildrenVo> children;

    @Setter
    @Getter
    public static class ChildrenVo {

        @Schema(description = "页面管理ID")
        private Integer decorateId;

        @Schema(description = "语言id")
        private Integer localeId;

        @Schema(description = "语言名称")
        private String language;
    }
}