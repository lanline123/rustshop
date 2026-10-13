// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.print.adapter;

import com.tigshop.bean.dto.print.adapter.BindResult;
import com.tigshop.bean.dto.print.adapter.PushResult;
import com.tigshop.bean.dto.print.adapter.UnbindResult;
import com.tigshop.bean.model.print.Print;

/**
 * 打印平台适配器契约
 * <p>
 * 把"飞鹅云/商米云"等不同厂商的打印机对接能力抽象为统一的三方法调用，
 * 让上层（{@code PrintServiceImpl}、{@code RabbitMqConsumer}）按 {@code platform} 分发。
 * <ul>
 *     <li>{@link #bind(Print)}    - 创建 / 编辑打印机时向平台注册设备（含鉴权验证）</li>
 *     <li>{@link #push(Print, String, Integer)} - 自动 / 手动打印时推送内容</li>
 *     <li>{@link #unbind(Print)}  - 删除打印机时同步解绑云端</li>
 * </ul>
 * 每个实现类通过 {@link #getPlatformCode()} 声明对应的 {@code PrintPlatformEnum.code}，
 * 由 {@link PrintPlatformAdapterFactory} 根据 {@code Print.platform} 路由。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
public interface PrintPlatformAdapter {

    /**
     * 返回适配器对应的平台编码（同 {@code PrintPlatformEnum#getCode()}）
     */
    Integer getPlatformCode();

    /**
     * 将打印机注册 / 绑定到平台
     * <p>
     * 本方法成功即视为"鉴权 + 绑定"通过（REQ-002）。
     *
     * @param print 打印机对象，含 {@code thirdAccount / thirdKey / printSn / printKey / printName / shopId} 等字段
     * @return 绑定结果；失败时 {@code success=false} 且带 errorCode / errorMsg
     */
    BindResult bind(Print print);

    /**
     * 向打印机推送一次打印任务
     *
     * @param print     打印机
     * @param orderId   订单 ID（商米 pushContent 必传作为幂等键；飞鹅实现忽略）
     * @param content   平台特定格式的打印内容（飞鹅：富文本标签；商米：UTF-8 hex + ESC/POS）
     * @param copies    打印联数（至少 1）
     * @param mediaText 可选的语音播报文本（TTS）。
     *                  <ul>
     *                      <li>商米实现：写入 {@code pushContent.media_text}，机器 TTS 播报</li>
     *                      <li>飞鹅实现：<b>忽略</b>——飞鹅打印机硬件自带喇叭，
     *                          收到打印任务会自动"叮咚+播报金额"，API 无法控制</li>
     *                      <li>{@code null} / 空串：不播报</li>
     *                  </ul>
     *                  当前仅在自动打印（{@code PrintTriggerTypeEnum.AUTO}）场景由 consumer 生成，
     *                  手动补打传 {@code null}。
     * @return 推送结果；{@link PushResult#getMappedStatus()} 返回 PrintLogStatusEnum 的 1 / 3 / 4
     */
    PushResult push(Print print, String orderId, String content, Integer copies, String mediaText);

    /**
     * 向后兼容壳：不带语音文本的旧签名。等价于 {@code push(print, orderId, content, copies, null)}。
     */
    default PushResult push(Print print, String orderId, String content, Integer copies) {
        return push(print, orderId, content, copies, null);
    }

    /**
     * 解绑 / 注销打印机
     * <p>
     * 商米实现：调 {@code unbindShop}；失败不得本地删（DC-02=A 强一致）。
     * 飞鹅实现：本地直删（strict_compat 保留现状）。
     *
     * @param print 打印机
     * @return 解绑结果
     */
    UnbindResult unbind(Print print);
}
