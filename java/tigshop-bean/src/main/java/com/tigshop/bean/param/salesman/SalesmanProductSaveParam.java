// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.salesman;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.tigshop.bean.model.salesman.SalesmanProduct;
import com.tigshop.bean.vo.salesman.SalesmanProductVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分销商品创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "分销商品参数")
public class SalesmanProductSaveParam {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "是否参与分销，0为不参与，1为参与")
    private Boolean isJoin;

    @Schema(description = "佣金计算方式，1为默认，2为自定义比例，3为自定义金额")
    private Integer commissionType;

    @Schema(description = "佣金数据")
    private List<SalesmanProductVO.CommissionData> commissionData;

    public SalesmanProduct createSalesmanProduct() {

        String commissionData = JSON.toJSONString(null);
        if (CollUtil.isNotEmpty(this.commissionData)) {
            commissionData = JSON.toJSONString(this.commissionData);
        }

        Long addTime = StringUtils.getCurrentTime();
        Integer shopId = HeaderUtils.getShopId();

        return SalesmanProduct.builder()
                .productId(this.productId)
                .isJoin(this.isJoin ? Constants.YES : Constants.NO)
                .commissionType(this.commissionType)
                .commissionData(commissionData)
                .addTime(addTime)
                .updateTime(addTime)
                .shopId(shopId)
                .build();
    }

}
