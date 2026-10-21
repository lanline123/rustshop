// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.feign.feieyun;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 飞鹅云添加打印机响应类
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "飞鹅云添加打印机响应类")
public class AddPrinterResult extends FeieyunApiResult<AddPrinterResult.AddPrinterData> {

    @Data
    @Schema(description = "添加打印机响应数据")
    public static class AddPrinterData {
        @Schema(description = "添加成功的打印机列表")
        private List<String> ok;

        @Schema(description = "添加失败的打印机列表")
        private List<String> no;
    }
}