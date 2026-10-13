// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.print;

import cn.hutool.json.JSONUtil;
import com.tigshop.bean.constant.print.PrintTemplateConstant;
import com.tigshop.bean.enums.print.PrintConfigTypeEnum;
import com.tigshop.bean.enums.print.PrintPaperSizeEnum;
import com.tigshop.bean.model.print.PrintConfig;
import com.tigshop.bean.param.print.PrintTemplate;
import com.tigshop.bean.param.print.PrintTemplateItem;
import com.tigshop.bean.param.print.PrintTemplateOption;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.tigshop.bean.constant.print.PrintTemplateConstant.createOption;
import static com.tigshop.bean.constant.print.PrintTemplateConstant.createTemplateItem;


/**
 * 打印配置VO
 *
 * @author Tigshop团队
 * @since 2025/7/22
 */
@Data
@NoArgsConstructor
public class PrintConfigVO {

    @Schema(description = "配置ID")
    private Integer id;

    @Schema(description = "打印机ID")
    private Integer printId;

    @Schema(description = "打印模板")
    private PrintTemplate template;

    @Schema(description = "配置类型")
    private Integer type;

    @Schema(description = "配置类型描述")
    private String typeDesc;

    @Schema(description = "纸张尺寸（JSON 数字输出，58 / 80），默认 58",
            implementation = Integer.class, example = "58")
    private PrintPaperSizeEnum paperSize;

    @Schema(description = "纸张尺寸描述，例如 58mm / 80mm")
    private String paperSizeDesc;

    @Schema(description = "创建时间")
    private String addTime;

    @Schema(description = "更新时间")
    private String updateTime;

    public PrintConfigVO(PrintConfig printConfig) {
        this.id = printConfig.getId();
        this.printId = printConfig.getPrintId();
        this.type = printConfig.getType();
        this.typeDesc = PrintConfigTypeEnum.getDescByCode(printConfig.getType());
        // 纸张尺寸：null 兼容为 58mm（历史数据读出来的可能是 null）
        PrintPaperSizeEnum paper = printConfig.getPaperSize() == null
                ? PrintPaperSizeEnum.SIZE_58 : printConfig.getPaperSize();
        this.paperSize = paper;
        this.paperSizeDesc = paper.getDesc();
        this.addTime = TigUtils.handelTime(printConfig.getAddTime());
        this.updateTime = TigUtils.handelTime(printConfig.getUpdateTime());

        // 将JSON字符串转换为PrintTemplate对象
        try {
            if (printConfig.getTemplate() != null && !printConfig.getTemplate().isEmpty()) {
                this.template = JSONUtil.parseArray(printConfig.getTemplate()).toBean(PrintTemplate.class);
                // 判断是否包含订单二维码，没有则补上，前端按这个接口进行渲染
                fillQRCode();
                fillMobile();
            }
        } catch (Exception e) {
            // 转换失败时，创建一个默认的PrintTemplate对象
            this.template = PrintTemplateConstant.createDefaultReceiptTemplate();
        }
    }

    private void fillMobile() {
        for (PrintTemplateItem item : template) {
            if ("配送信息".equals(item.getTitle())) {
                boolean hasMobile = false;
                for (PrintTemplateOption option : item.getOptions()) {
                    if ("手机号模糊".equals(option.getChooseTitle())) {
                        hasMobile = true;
                    }
                }
                if (!hasMobile) {
                    item.getOptions().add(createOption("手机号模糊", false));
                }
            }
        }
    }

    /**
     * 填充二维码 选项（兼容老版本）
     */
    private void fillQRCode() {
        boolean hasQRCode = false;
        for (PrintTemplateItem item : template) {
            if ("二维码".equals(item.getTitle())) {
                hasQRCode = true;
                break;
            }
        }
        if (!hasQRCode) {
            // 查找“其他订单信息”的位置
            int insertIndex = -1;
            for (int i = 0; i < template.size(); i++) {
                if ("其他订单信息".equals(template.get(i).getTitle())) {
                    insertIndex = i;
                    break;
                }
            }

            // 创建二维码模板项
            if (insertIndex != -1 && insertIndex + 1 <= template.size()) {
                template.add(insertIndex + 1, createTemplateItem("二维码",
                        createOption("订单二维码", true)
                ));
            }
        }
    }
}