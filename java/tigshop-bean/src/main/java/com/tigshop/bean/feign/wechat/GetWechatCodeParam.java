// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.feign.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 获取小程序二维码入参
 *
 * @author Tigshop团队
 * @create 2025年01月23日 16:30
 */
@Data
public class GetWechatCodeParam {
    @Schema(description = "场景值，最大32个可见字符，只支持数字")
    private String scene;
    @Schema(description = "默认是主页，页面 page，例如 pages/index/index")
    private String page;
    @Schema(description = "默认是true，检查page 是否存在")
    private Boolean check_path;
    @Schema(description = "默认是release，指定二维码的版本，正式版为 \"release\"，体验版为 \"trial\"，开发版为 \"develop\"")
    private String env_version;
    @Schema(description = "默认是430，二维码的宽度")
    private Integer width;
    @Schema(description = "自动配置线条颜色，如果颜色依然是黑色")
    private Boolean auto_color;
    @Schema(description = "默认是{\"r\":0,\"g\":0,\"b\":0} 。auto_color 为 false 时生效")
    private Map<String, Integer> line_color;
    @Schema(description = "默认是false，是否需要透明底色，为 true 时，生成透明底色的小程序")
    private Boolean is_hyaline;

    public GetWechatCodeParam(String scene, String page) {
        this.scene = scene;
        this.page = page;
    }
}