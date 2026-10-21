// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.finance;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "财务总览响应对象")
@Getter
@Setter
public class AccountPanelResVO<F> {

    @Schema(description = "数据列表")
    private AccountPanelVO filterResult;

    @Schema(description = "筛选条件")
    private F filter;

    public AccountPanelResVO(AccountPanelVO item, F filter) {
        this.filterResult = item;
        this.filter = filter;
    }
}
