// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.constant.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tigshop.bean.param.print.PrintTemplate;
import com.tigshop.bean.param.print.PrintTemplateItem;
import com.tigshop.bean.param.print.PrintTemplateOption;

import java.util.ArrayList;
import java.util.List;

/**
 * 打印模板常量
 *
 * @author Tigshop团队
 * @since 2025/7/22
 */
public class PrintTemplateConstant {

    /**
     * 默认购物小票模板
     * 包含：
     * - 小票头部（商家名称、门店名称）
     * - 配送信息（配送方式、收货信息）
     */
    public static final String DEFAULT_RECEIPT_TEMPLATE;

    static {
        try {
            DEFAULT_RECEIPT_TEMPLATE = new ObjectMapper().writeValueAsString(createDefaultReceiptTemplate());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("初始化默认打印模板失败", e);
        }
    }

    /**
     * 创建默认购物小票模板
     *
     * @return 默认购物小票模板对象
     */
    public static PrintTemplate createDefaultReceiptTemplate() {
        PrintTemplate template = new PrintTemplate();

        template.add(createTemplateItem("小票头部",
                createOption("商家名称", true)
//                createOption("门店名称", true)
        ));

        template.add(createTemplateItem("配送信息",
                createOption("配送方式", true),
                createOption("商配&同城-收货信息", true),
                createOption("手机号模糊", true)
        ));

        template.add(createTemplateItem("买家备注",
                createOption("买家备注", true)
        ));

        template.add(createTemplateItem("商品信息",
                createOption("商品基础信息", true),
                createOption("规格编码", true)
        ));

        template.add(createTemplateItem("运费信息",
                createOption("运费", true)
        ));

        template.add(createTemplateItem("优惠信息",
                createOption("优惠明细", true),
                createOption("优惠总计", true)
        ));

        template.add(createTemplateItem("支付信息",
                createOption("实收金额", true),
                createOption("支付方式", true),
                createOption("第三方支付单号", true)
        ));

        template.add(createTemplateItem("客户信息",
                createOption("客户信息", true)
        ));

        template.add(createTemplateItem("其他订单信息",
                createOption("下单时间", true),
                createOption("支付时间", true),
                createOption("渠道类型", true)
//            createOption("门店店址", true, "门店店址")
        ));

        template.add(createTemplateItem("二维码",
                createOption("订单二维码", true)
        ));

        template.add(createTemplateItem("底部公告",
                createOption("底部公告", true, "感谢您的惠顾，欢迎再次光临！")
        ));

        return template;
    }

    /**
     * 创建模板项
     *
     * @param title   模板项标题
     * @param options 模板项选项数组
     * @return 模板项对象
     */
    public static PrintTemplateItem createTemplateItem(String title, PrintTemplateOption... options) {
        PrintTemplateItem item = new PrintTemplateItem();
        item.setTitle(title);

        List<PrintTemplateOption> optionList = new ArrayList<>();
        for (PrintTemplateOption option : options) {
            optionList.add(option);
        }
        item.setOptions(optionList);

        return item;
    }

    /**
     * 创建模板选项
     *
     * @param chooseTitle   选项标题
     * @param choose        是否选中
     * @return 模板选项对象
     */
    public static PrintTemplateOption createOption(String chooseTitle, boolean choose) {
        return createOption(chooseTitle, choose, "");
    }

    /**
     * 创建模板选项
     *
     * @param chooseTitle   选项标题
     * @param choose        是否选中
     * @param value         实际值
     * @return 模板选项对象
     */
    private static PrintTemplateOption createOption(String chooseTitle, boolean choose, String value) {
        PrintTemplateOption option = new PrintTemplateOption();
        option.setChooseTitle(chooseTitle);
        option.setChoose(choose);
        option.setValue(value);
        return option;
    }
}