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

/**
 * 飞鹅云API基础响应类
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "飞鹅云API基础响应类")
public class FeieyunApiResult<T> {
    @Schema(description = "返回消息")
    private String msg;

    @Schema(description = "返回码，0表示成功，非0表示失败")
    private Integer ret;

    @Schema(description = "返回数据")
    private T data;

    @Schema(description = "服务器执行时间（毫秒）")
    private Integer serverExecutedTime;

    /**
     * 判断请求是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return ret != null && ret == 0;
    }
}