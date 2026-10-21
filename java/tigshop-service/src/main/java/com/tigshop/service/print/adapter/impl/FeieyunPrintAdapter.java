// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.print.adapter.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.dto.print.adapter.BindResult;
import com.tigshop.bean.dto.print.adapter.PushResult;
import com.tigshop.bean.dto.print.adapter.UnbindResult;
import com.tigshop.bean.enums.print.PrintLogStatusEnum;
import com.tigshop.bean.enums.print.PrintPlatformEnum;
import com.tigshop.bean.feign.feieyun.AddPrinterResult;
import com.tigshop.bean.feign.feieyun.FeieyunApiResult;
import com.tigshop.bean.model.print.Print;
import com.tigshop.common.utils.FeieyunApiUtils;
import com.tigshop.feign.FeieyunApiClient;
import com.tigshop.service.print.adapter.PrintPlatformAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 飞鹅云打印适配器
 * <p>
 * <b>strict_compat 守护</b>：本类仅包装 {@link FeieyunApiClient} / {@link FeieyunApiUtils} /
 * {@link FeieyunApiResult} / {@link AddPrinterResult}，不修改飞鹅既有 4 个类的任何一行代码。
 * 调用参数与原 {@code PrintServiceImpl.create / update / RabbitMqConsumer.receivePrintOrderMessage}
 * 中的写法保持完全一致，保证字节级等价。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeieyunPrintAdapter implements PrintPlatformAdapter {

    private static final String API_NAME_ADD = "Open_printerAddlist";
    private static final String API_NAME_PRINT = "Open_printMsg";
    private static final String API_NAME_DEL = "Open_printerDelList";

    private final FeieyunApiClient feieyunApiClient;

    @Override
    public Integer getPlatformCode() {
        return PrintPlatformEnum.FEIEYUN.getCode();
    }

    @Override
    public BindResult bind(Print print) {
        try {
            String stime = FeieyunApiUtils.getCurrentTimestamp();
            String sig = FeieyunApiUtils.generateSign(print.getThirdAccount(), print.getThirdKey(), stime);
            String printerContent = print.getPrintSn() + "#" + print.getPrintKey() + "#" + print.getPrintName();

            String raw = feieyunApiClient.addPrinter(
                    print.getThirdAccount(), stime, sig, API_NAME_ADD, printerContent);

            AddPrinterResult result = JSONUtil.parseObj(raw).toBean(AddPrinterResult.class);

            // 复用 PrintServiceImpl.create 原判定：API 成功 && (ok 非空 || no 首条含"已被添加过")
            if (result != null && result.isSuccess()
                    && (!result.getData().getOk().isEmpty()
                    || (!result.getData().getNo().isEmpty()
                    && result.getData().getNo().getFirst().contains("已被添加过")))) {
                return BindResult.builder().success(true).rawResponse(raw).build();
            }

            // 失败信息映射（与原代码一致的错误提取顺序）
            String errorMsg = "添加打印机失败";
            if (result != null) {
                if (ObjectUtil.notEqual(result.getMsg(), "ok")) {
                    errorMsg = result.getMsg();
                } else {
                    List<String> no = result.getData() == null ? null : result.getData().getNo();
                    if (no != null && !no.isEmpty()) {
                        errorMsg = no.getFirst();
                    }
                }
            }
            return BindResult.builder()
                    .success(false)
                    .errorCode(result != null && result.getRet() != null ? String.valueOf(result.getRet()) : null)
                    .errorMsg(errorMsg)
                    .rawResponse(raw)
                    .build();
        } catch (Exception e) {
            log.error("飞鹅云绑定打印机异常：{}", print.getPrintSn(), e);
            return BindResult.builder().success(false).errorMsg("飞鹅云绑定打印机异常：" + e.getMessage()).build();
        }
    }

    @Override
    public PushResult push(Print print, String orderId, String content, Integer copies, String mediaText) {
        // 飞鹅云调用不需要 orderId 作为幂等键，此参数仅商米路径使用，此处忽略
        // mediaText 暂不处理（飞鹅云语音播报功能预留，当前不拼 <PLUGIN>）
        try {
            int times = copies == null || copies <= 0 ? 1 : copies;
            String stime = FeieyunApiUtils.getCurrentTimestamp();
            String sig = FeieyunApiUtils.generateSign(print.getThirdAccount(), print.getThirdKey(), stime);

            String finalContent = content;

            String raw = feieyunApiClient.printOrder(
                    print.getThirdAccount(), stime, sig, API_NAME_PRINT,
                    print.getPrintSn(), finalContent, times);

            FeieyunApiResult result = JSONUtil.parseObj(raw).toBean(FeieyunApiResult.class);

            if (result != null && result.isSuccess()) {
                return PushResult.builder()
                        .success(true)
                        .mappedStatus(PrintLogStatusEnum.PUSHED.getCode())
                        .rawResponse(raw)
                        .build();
            }
            return PushResult.builder()
                    .success(false)
                    .mappedStatus(PrintLogStatusEnum.PUSH_FAILED.getCode())
                    .errorCode(result != null && result.getRet() != null ? String.valueOf(result.getRet()) : null)
                    .errorMsg(result == null ? "飞鹅云返回为空" : result.getMsg())
                    .rawResponse(raw)
                    .build();
        } catch (Exception e) {
            log.error("飞鹅云推送打印异常：{}", print.getPrintSn(), e);
            return PushResult.builder()
                    .success(false)
                    .mappedStatus(PrintLogStatusEnum.PUSH_FAILED.getCode())
                    .errorMsg("飞鹅云推送异常：" + e.getMessage())
                    .build();
        }
    }

    @Override
    public UnbindResult unbind(Print print) {
        // 与既有 PrintServiceImpl.del 等价：strict_compat 保持"本地直删"行为，
        // 此方法提供以供 PrintServiceImpl 改造后按 platform 分发时调用；
        // 但对飞鹅分支，PrintServiceImpl.del 不会调用本方法（保留原 del 逻辑）。
        // 保留此实现是为了当 update 分支需要"先删旧 SN"时复用（等价 PrintServiceImpl.update 251-246 行现有调用）
        try {
            String stime = FeieyunApiUtils.getCurrentTimestamp();
            String sig = FeieyunApiUtils.generateSign(print.getThirdAccount(), print.getThirdKey(), stime);

            String raw = feieyunApiClient.deletePrinter(
                    print.getThirdAccount(), stime, sig, API_NAME_DEL, print.getPrintSn());

            FeieyunApiResult result = JSONUtil.parseObj(raw).toBean(FeieyunApiResult.class);

            if (result != null && result.isSuccess()) {
                return UnbindResult.builder().success(true).rawResponse(raw).build();
            }
            return UnbindResult.builder()
                    .success(false)
                    .errorCode(result != null && result.getRet() != null ? String.valueOf(result.getRet()) : null)
                    .errorMsg(result == null ? "飞鹅云返回为空" : result.getMsg())
                    .rawResponse(raw)
                    .build();
        } catch (Exception e) {
            log.error("飞鹅云解绑打印机异常：{}", print.getPrintSn(), e);
            return UnbindResult.builder().success(false).errorMsg("飞鹅云解绑异常：" + e.getMessage()).build();
        }
    }
}
