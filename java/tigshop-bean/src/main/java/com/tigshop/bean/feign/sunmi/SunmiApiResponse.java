// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.feign.sunmi;

import lombok.Data;

/**
 * 商米云打印 OpenAPI 通用响应包装
 * <p>
 * 参考 <a href="https://docs.sunmi.com/zh-CN/cdixeghjk491/xffdeghjk524">商米云打印开发对接 API 接口</a>。
 * 商米响应体统一结构：{@code {"code": 1, "msg": "...", "data": ...}}；
 * {@code code == 1} 视为成功，其它视为业务失败。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Data
public class SunmiApiResponse<T> {

    /**
     * 业务码。1 表示成功，其它表示业务失败；
     * 网关异常（30000/30001）无需验签
     */
    private Integer code;

    /**
     * 业务消息
     */
    private String msg;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 是否业务成功
     */
    public boolean isSuccess() {
        return code != null && code == 1;
    }
}
