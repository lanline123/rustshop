// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.print;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 打印模板选项
 *
 * @author Tigshop团队
 * @since 2025/7/22
 */
@Data
public class PrintTemplateOption {

    @Schema(description = "选项标题")
    private String chooseTitle;

    @Schema(description = "是否选中")
    private Boolean choose;

    @Schema(description = "实际值")
    private String value;


    public static boolean isChoose(List<PrintTemplateOption> options, String chooseTitle) {
        if (options == null) {
            return false;
        }
        for (PrintTemplateOption option : options) {
            if (option.getChoose() && chooseTitle.equals(option.getChooseTitle())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否选中任意一个指定标题
     *
     * @param options      选项列表
     * @param chooseTitles 可选的标题（可变参数）
     * @return 是否选中任意一个
     */
    public static boolean isChooseAny(List<PrintTemplateOption> options, String... chooseTitles) {
        if (options == null || chooseTitles == null || chooseTitles.length == 0) {
            return false;
        }
        for (PrintTemplateOption option : options) {
            if (option.getChoose()) {
                for (String title : chooseTitles) {
                    if (title.equals(option.getChooseTitle())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}