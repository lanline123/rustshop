package com.tigshop.service.print.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.print.adapter.BindResult;
import com.tigshop.bean.dto.print.adapter.UnbindResult;
import com.tigshop.bean.enums.print.PrintPaperSizeEnum;
import com.tigshop.bean.enums.print.PrintPlatformEnum;
import com.tigshop.bean.enums.print.PrintStatusEnum;
import com.tigshop.bean.feign.feieyun.AddPrinterResult;
import com.tigshop.bean.feign.feieyun.FeieyunApiResult;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.model.print.PrintConfig;
import com.tigshop.bean.param.print.PrintAddParam;
import com.tigshop.bean.param.print.PrintUpdateParam;
import com.tigshop.bean.query.print.PrintPageQuery;
import com.tigshop.bean.vo.print.PrintDetailVO;
import com.tigshop.bean.vo.print.PrintPageVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.FeieyunApiUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.feign.FeieyunApiClient;
import com.tigshop.mapper.print.PrintConfigMapper;
import com.tigshop.mapper.print.PrintMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.print.PrintConfigService;
import com.tigshop.service.print.PrintPosDeviceService;
import com.tigshop.service.print.PrintRoutingService;
import com.tigshop.service.print.PrintService;
import com.tigshop.service.print.adapter.PrintPlatformAdapter;
import com.tigshop.service.print.adapter.PrintPlatformAdapterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Admin
 * @description 针对表【print(打印机表)】的数据库操作Service实现
 * @createDate 2025-07-22 09:22:01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrintServiceImpl extends BaseServiceImpl<PrintMapper, Print> implements PrintService {

    private final FeieyunApiClient feieyunApiClient;
    private final PrintConfigMapper printConfigMapper;
    private final PrintConfigService printConfigService;
    private final PrintPlatformAdapterFactory adapterFactory;
    private final PrintPosDeviceService bindingService;
    private final PrintRoutingService routingService;

    @Override
    public Page<PrintPageVO> list(PrintPageQuery pageQuery) {
        // 分页
        Page<Print> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Print> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Print::getShopId, getShopId());

        // 排序字段
        buildSortOrder(page, pageQuery.getSortField(), pageQuery.getSortOrder());

        // 搜索条件
        if (StringUtils.isNotEmpty(pageQuery.getPrintName())) {
            queryWrapper.like(Print::getPrintName, pageQuery.getPrintName());
        }
        if (StringUtils.isNotEmpty(pageQuery.getPrintSn())) {
            queryWrapper.like(Print::getPrintSn, pageQuery.getPrintSn());
        }
        if (pageQuery.getStatus() != null) {
            queryWrapper.eq(Print::getStatus, pageQuery.getStatus());
        }
        if (pageQuery.getPlatform() != null) {
            queryWrapper.eq(Print::getPlatform, pageQuery.getPlatform());
        }
        if (StringUtils.isNotBlank(pageQuery.getDeviceCode())) {
            queryWrapper.inSql(Print::getPrintId,
                    "select print_id from print_pos_device where shop_id = " + getShopId()
                            + " and device_code = '" + pageQuery.getDeviceCode().replace("'", "''") + "'");
        }
        if(StringUtils.isNotBlank(pageQuery.getKeyword())) {
            //打印机名称和打印编号
            queryWrapper.and((consumer)->{
                consumer.like(Print::getPrintName, pageQuery.getKeyword())
                        .or()
                        .like(Print::getPrintSn, pageQuery.getKeyword());
            });
        }

        // 执行查询
        Page<Print> printPage = this.page(page, queryWrapper);

        // 转换为VO，并回填 paperSize（从 print_config 批量查一次）
        Map<Integer, PrintPaperSizeEnum> paperByPrintId = loadPaperByPrintIds(
                printPage.getRecords().stream().map(Print::getPrintId).collect(Collectors.toSet()));
        Map<Integer, String> bindings = bindingService.bindingsByPrintIds(getShopId(),
                printPage.getRecords().stream().map(Print::getPrintId).toList());

        Page<PrintPageVO> result = new Page<>(printPage.getCurrent(), printPage.getSize(), printPage.getTotal());
        result.setRecords(printPage.getRecords().stream()
                .map(p -> {
                    PrintPageVO vo = new PrintPageVO(p);
                    PrintPaperSizeEnum paper = paperByPrintId.getOrDefault(p.getPrintId(), PrintPaperSizeEnum.SIZE_58);
                    vo.setPaperSize(paper);
                    vo.setPaperSizeDesc(paper.getDesc());
                    vo.setDeviceCode(bindings.get(p.getPrintId()));
                    return vo;
                })
                .toList());

        return result;
    }

    /**
     * 批量加载 printId → paperSize 映射。同一 printId 多个 config 取第一个。
     * DB 返回 null 时兜底为 {@link PrintPaperSizeEnum#SIZE_58}。
     */
    private Map<Integer, PrintPaperSizeEnum> loadPaperByPrintIds(Set<Integer> printIds) {
        if (printIds == null || printIds.isEmpty()) {
            return Map.of();
        }
        List<PrintConfig> configs = printConfigMapper.selectList(new LambdaQueryWrapper<PrintConfig>()
                .in(PrintConfig::getPrintId, printIds)
                .select(PrintConfig::getPrintId, PrintConfig::getPaperSize));
        return configs.stream().collect(Collectors.toMap(
                PrintConfig::getPrintId,
                c -> c.getPaperSize() == null ? PrintPaperSizeEnum.SIZE_58 : c.getPaperSize(),
                (existing, replacement) -> existing));
    }

    @Override
    public PrintDetailVO detail(Integer id) {
        Print print = this.lambdaQuery()
                .eq(Print::getPrintId, id)
                .eq(Print::getShopId, getShopId())
                .one();

        if (print == null) {
            throw new GlobalException("打印机不存在");
        }

        PrintDetailVO vo = new PrintDetailVO(print);
        // 回填纸张尺寸：从 print_config 查首个 config 的 paperSize，null 兜底为 58mm
        PrintConfig anyConfig = printConfigMapper.selectOne(new LambdaQueryWrapper<PrintConfig>()
                .eq(PrintConfig::getPrintId, id)
                .last(" limit 1")
                .select(PrintConfig::getPaperSize));
        PrintPaperSizeEnum paper = (anyConfig == null || anyConfig.getPaperSize() == null)
                ? PrintPaperSizeEnum.SIZE_58 : anyConfig.getPaperSize();
        vo.setPaperSize(paper);
        vo.setPaperSizeDesc(paper.getDesc());
        vo.setDeviceCode(bindingService.bindingsByPrintIds(getShopId(), List.of(id))
                .get(id));
        return vo;
    }

    @Override
    @Transactional
    public boolean create(PrintAddParam printAddParam) {
        // 平台分发：商米云走 adapter 路径，飞鹅云保留既有实现（strict_compat）
        Integer platform = printAddParam.getPlatform() == null
                ? PrintPlatformEnum.FEIEYUN.getCode()
                : printAddParam.getPlatform();
        if (PrintPlatformEnum.SUNMI.getCode().equals(platform)) {
            return createSunmi(printAddParam);
        }
        // ==== 以下为飞鹅云既有实现，逻辑零修改 ====
        try {
            // 生成时间戳
            String stime = FeieyunApiUtils.getCurrentTimestamp();
            // 生成签名
            String sig = FeieyunApiUtils.generateSign(printAddParam.getThirdAccount(), printAddParam.getThirdKey(), stime);
            // 构建打印机信息
            String printerContent = printAddParam.getPrintSn() + "#" + printAddParam.getPrintKey() + "#" + printAddParam.getPrintName();

            // 调用飞鹅云API添加打印机
            String resultStr = feieyunApiClient.addPrinter(
                    printAddParam.getThirdAccount(),
                    stime,
                    sig,
                    "Open_printerAddlist",
                    printerContent
            );

            AddPrinterResult result = JSONUtil.parseObj(resultStr).toBean(AddPrinterResult.class);

            // 检查API调用结果
            if (result != null && result.isSuccess() && (!result.getData().getOk().isEmpty() || result.getData().getNo().getFirst().contains("已被添加过"))) {
                // API调用成功，保存打印机信息到数据库
                Print print = new Print();
                print.setPrintName(printAddParam.getPrintName());
                print.setPrintSn(printAddParam.getPrintSn());
                print.setPrintKey(printAddParam.getPrintKey());
                print.setThirdAccount(printAddParam.getThirdAccount());
                print.setThirdKey(printAddParam.getThirdKey());
                print.setPrintNumber(1);
                print.setPlatform(PrintPlatformEnum.FEIEYUN.getCode());
                print.setShopId(getShopId());
                print.setStatus(printAddParam.getStatus() == null ? PrintStatusEnum.DISABLED.getCode() : printAddParam.getStatus());
                print.setAutoPrint(printAddParam.getAutoPrint());
                // 语音开关：前端未传默认 1（开启，兼容历史行为）
                print.setVoiceEnabled(printAddParam.getVoiceEnabled() == null ? 1 : printAddParam.getVoiceEnabled());
                // 语音内容类型：前端未传默认 0（订单号+金额）
                print.setVoiceContentType(printAddParam.getVoiceContentType() == null ? 0 : printAddParam.getVoiceContentType());
                print.setIsDefault(Integer.valueOf(1).equals(printAddParam.getIsDefault()) ? 1 : 0);
                print.setAddTime(StringUtils.getCurrentTime());
                print.setUpdateTime(StringUtils.getCurrentTime());
                clearOtherDefault(null, print.getIsDefault());

                boolean save = save(print);

                // 初始化默认的打印配置：前端未传则走 initConfig 内部默认（58mm）
                if (save) {
                    boolean initConfigResult = printConfigService.initConfig(
                            print.getPrintId(), printAddParam.getPaperSize());
                    if (!initConfigResult) {
                        log.warn("打印机配置初始化失败，打印机ID：{}", print.getPrintId());
                    }
                    bindingService.replace(getShopId(), print.getPrintId(), printAddParam.getDeviceCode());
                }

                return save;
            } else {
                // API调用失败
                log.error("添加打印机失败，API返回结果：{}", resultStr);
                if (result == null) {
                    throw new GlobalException("添加打印机失败");
                }
                if (ObjectUtil.notEqual(result.getMsg(), "ok")) {
                    throw new GlobalException(result.getMsg());
                }
                if (ObjectUtil.isNotEmpty(result.getData().getNo())) {
                    throw new GlobalException(result.getData().getNo().getFirst());
                }
                throw new GlobalException("添加打印机失败");
            }
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobalException("添加打印机失败");
        }
    }

    @Override
    @Transactional
    public boolean update(PrintUpdateParam printUpdateParam) {
        // 先取 oldPrint 判断平台，再按平台分发
        Print oldPrint = this.lambdaQuery()
                .eq(Print::getPrintId, printUpdateParam.getPrintId())
                .eq(Print::getShopId, getShopId())
                .one();

        if (oldPrint == null) {
            throw new GlobalException("打印机不存在");
        }
        if (PrintPlatformEnum.SUNMI.getCode().equals(oldPrint.getPlatform())) {
            return updateSunmi(printUpdateParam, oldPrint);
        }
        // ==== 以下为飞鹅云既有实现，逻辑零修改 ====
        try {
            //DB加密后的数据
            String dbMaskAfterPrintSn = StringUtils.maskMiddleHalf(oldPrint.getPrintSn());
            String dbMaskAfterPrintKey = StringUtils.maskMiddleHalf(oldPrint.getPrintKey());
            String dbMaskAfterThirdKey = StringUtils.maskMiddleHalf(oldPrint.getThirdKey());

            boolean needUpdatePrinter;
            if (needUpdatePrinter = (!Objects.equals(dbMaskAfterPrintSn, printUpdateParam.getPrintSn())
                    || !Objects.equals(dbMaskAfterPrintKey, printUpdateParam.getPrintKey())
                    || !Objects.equals(dbMaskAfterThirdKey, printUpdateParam.getThirdKey()))) {
                //不相等用前端传入的数据
                printUpdateParam.setPrintSn(!Objects.equals(dbMaskAfterPrintSn, printUpdateParam.getPrintSn())
                        ? printUpdateParam.getPrintSn() : oldPrint.getPrintSn());
                printUpdateParam.setPrintKey(!Objects.equals(dbMaskAfterPrintKey, printUpdateParam.getPrintKey())
                        ? printUpdateParam.getPrintKey() : oldPrint.getPrintKey());
                printUpdateParam.setThirdKey(!Objects.equals(dbMaskAfterThirdKey, printUpdateParam.getThirdKey())
                        ? printUpdateParam.getThirdKey() : oldPrint.getThirdKey());
            }else{
                //相等的话用db数据(前端是*)
                printUpdateParam.setPrintSn(oldPrint.getPrintSn());
                printUpdateParam.setPrintKey(oldPrint.getPrintKey());
                printUpdateParam.setThirdKey(oldPrint.getThirdKey());
            }

            //需要更改打印机
            needUpdatePrinter = needUpdatePrinter || !oldPrint.getThirdAccount().equals(printUpdateParam.getThirdAccount());
            if(needUpdatePrinter){
                // 先删除原打印机
                String stime = FeieyunApiUtils.getCurrentTimestamp();
                String sig = FeieyunApiUtils.generateSign(oldPrint.getThirdAccount(), oldPrint.getThirdKey(), stime);
                String deleteResultStr = feieyunApiClient.deletePrinter(
                        oldPrint.getThirdAccount(),
                        stime,
                        sig,
                        "Open_printerDelList",
                        oldPrint.getPrintSn()
                );

                FeieyunApiResult deleteResult = JSONUtil.parseObj(deleteResultStr).toBean(FeieyunApiResult.class);

                if (deleteResult == null || !deleteResult.isSuccess()) {
                    // 继续执行，因为可能是打印机在飞鹅云平台已经不存在
                    log.error("删除原打印机失败，API返回结果：{}", deleteResultStr);
                }

                // 添加新打印机
                stime = FeieyunApiUtils.getCurrentTimestamp();
                sig = FeieyunApiUtils.generateSign(printUpdateParam.getThirdAccount(), printUpdateParam.getThirdKey(), stime);
                String printerContent = printUpdateParam.getPrintSn() + "#" + printUpdateParam.getPrintKey() + "#" + printUpdateParam.getPrintName();
                String resultStr = feieyunApiClient.addPrinter(
                        printUpdateParam.getThirdAccount(),
                        stime,
                        sig,
                        "Open_printerAddlist",
                        printerContent
                );

                AddPrinterResult addResult = JSONUtil.parseObj(resultStr).toBean(AddPrinterResult.class);

                if (addResult == null || !addResult.isSuccess() || addResult.getData().getOk().isEmpty()) {
                    log.error("添加新打印机失败，API返回结果：{}", resultStr);
                    throw new GlobalException("更新打印机失败：" + (addResult != null ? addResult.getMsg() : "调用API失败"));
                }
            }
            // 更新数据库中的打印机信息
            Print updatePrint = new Print();
            updatePrint.setPrintId(printUpdateParam.getPrintId());
            updatePrint.setPrintName(printUpdateParam.getPrintName());
            updatePrint.setPrintSn(printUpdateParam.getPrintSn());
            updatePrint.setPrintKey(printUpdateParam.getPrintKey());
            updatePrint.setThirdAccount(printUpdateParam.getThirdAccount());
            updatePrint.setThirdKey(printUpdateParam.getThirdKey());
            updatePrint.setPrintNumber(1);
            updatePrint.setUpdateTime(StringUtils.getCurrentTime());
            updatePrint.setStatus(printUpdateParam.getStatus());
            updatePrint.setAutoPrint(printUpdateParam.getAutoPrint());
            if (printUpdateParam.getIsDefault() != null) {
                updatePrint.setIsDefault(Integer.valueOf(1).equals(printUpdateParam.getIsDefault()) ? 1 : 0);
            }
            // 语音开关：前端传值才覆盖，null 保留原值（旧前端兼容）
            if (printUpdateParam.getVoiceEnabled() != null) {
                updatePrint.setVoiceEnabled(printUpdateParam.getVoiceEnabled());
            }
            // 语音内容类型：前端传值才覆盖，null 保留原值
            if (printUpdateParam.getVoiceContentType() != null) {
                updatePrint.setVoiceContentType(printUpdateParam.getVoiceContentType());
            }

            clearOtherDefault(updatePrint.getPrintId(), updatePrint.getIsDefault());
            boolean updated = this.updateById(updatePrint);
            // 纸张尺寸：前端传值时同步到 print_config（旧前端不传则保留历史值）
            if (updated && printUpdateParam.getPaperSize() != null) {
                printConfigService.updatePaperSizeByPrintId(
                        updatePrint.getPrintId(), printUpdateParam.getPaperSize());
            }
            if (updated && printUpdateParam.getDeviceCode() != null) {
                bindingService.replace(getShopId(), updatePrint.getPrintId(), printUpdateParam.getDeviceCode());
            }
            return updated;
        } catch (GlobalException globalException) {
            // 将异常抛出
            throw globalException;
        } catch (Exception e) {
            log.error("更新打印机异常", e);
            throw new GlobalException("更新打印机失败：" + e.getMessage());
        }
    }


    /**
     *
     * @param oldStr
     * @param newStr
     * @param returnStr
     * @return
     */
    private String equalReturnLastStr(String oldStr, String newStr, String returnStr) {
        if (Objects.equals(oldStr, newStr)) {
            //相等的话,说明直接返回
            return returnStr;
        }
        return newStr;
    }

    @Override
    @Transactional
    public boolean updateField(UpdateFieldDTO updateField, String[] allowFields) {
        if ("isDefault".equals(updateField.getField()) && "1".equals(updateField.getVal())) {
            clearOtherDefault(updateField.getId(), 1);
        }
        return super.updateField(updateField, allowFields);
    }

    @Override
    @Transactional
    public boolean del(Integer id) {
        // 查询打印机信息
        Print print = this.lambdaQuery()
                .eq(Print::getPrintId, id)
                .eq(Print::getShopId, getShopId())
                .one();

        if (print == null) {
            throw new GlobalException("打印机不存在");
        }
        // 商米云：先同步解绑云端（DC-02=A 强一致），失败不删本地
        if (PrintPlatformEnum.SUNMI.getCode().equals(print.getPlatform())) {
            UnbindResult r = adapterFactory.of(print.getPlatform()).unbind(print);
            if (r == null || !r.isSuccess()) {
                throw new GlobalException("商米云解绑失败：" + (r == null ? "返回为空" : r.getErrorMsg()));
            }
        }
        // 飞鹅云：保留原"本地直删、不调云端解绑"的既有行为（strict_compat）
        this.removeById(id);
        printConfigMapper.delete(new LambdaQueryWrapper<PrintConfig>().eq(PrintConfig::getPrintId, id));
        bindingService.replace(getShopId(), id, null);
        return true;
    }

    // ==========================================================================
    // 商米云分支实现（新增）
    // ==========================================================================

    /**
     * 商米云新增打印机
     * <p>
     * 通过 {@link PrintPlatformAdapter#bind(Print)} 向商米云绑定店铺（bindShop）；
     * 成功后保存打印机并初始化默认模板配置。
     */
    private boolean createSunmi(PrintAddParam param) {
        Print print = new Print();
        print.setPrintName(param.getPrintName());
        print.setPrintSn(param.getPrintSn());
        print.setPrintKey(param.getPrintKey()); // 商米无此字段概念，允许为空
        print.setThirdAccount(param.getThirdAccount());
        print.setThirdKey(param.getThirdKey());
        print.setPrintNumber(1);
        print.setPlatform(PrintPlatformEnum.SUNMI.getCode());
        print.setShopId(getShopId());
        print.setStatus(param.getStatus() == null ? PrintStatusEnum.DISABLED.getCode() : param.getStatus());
        print.setAutoPrint(param.getAutoPrint());
        // 语音开关：前端未传默认 1（开启）
        print.setVoiceEnabled(param.getVoiceEnabled() == null ? 1 : param.getVoiceEnabled());
        // 语音内容类型：前端未传默认 0（订单号+金额）
        print.setVoiceContentType(param.getVoiceContentType() == null ? 0 : param.getVoiceContentType());
        print.setIsDefault(Integer.valueOf(1).equals(param.getIsDefault()) ? 1 : 0);
        print.setAddTime(StringUtils.getCurrentTime());
        print.setUpdateTime(StringUtils.getCurrentTime());
        clearOtherDefault(null, print.getIsDefault());

        // 先调云端鉴权 + 绑定（REQ-002：鉴权失败严禁落库）
        BindResult r = adapterFactory.of(PrintPlatformEnum.SUNMI.getCode()).bind(print);
        if (r == null || !r.isSuccess()) {
            log.error("商米云绑定失败：sn={}, resp={}", param.getPrintSn(), r == null ? "null" : r.getRawResponse());
            throw new GlobalException(r == null ? "商米云绑定失败" : r.getErrorMsg());
        }

        boolean saved = save(print);
        if (saved) {
            boolean initConfigResult = printConfigService.initConfig(
                    print.getPrintId(), param.getPaperSize());
            if (!initConfigResult) {
                log.warn("打印机配置初始化失败，打印机ID：{}", print.getPrintId());
            }
            if (param.getDeviceCode() != null) {
                bindingService.replace(getShopId(), print.getPrintId(), param.getDeviceCode());
            }
        }
        return saved;
    }

    /**
     * 商米云编辑打印机
     * <p>
     * 若核心字段（账号/SN/秘钥）变更，则先解绑旧打印机，再绑定新打印机。
     */
    private boolean updateSunmi(PrintUpdateParam param, Print oldPrint) {
        // 处理前端脱敏回填：* 代表未变更，使用 DB 值
        String dbMaskAfterPrintSn = StringUtils.maskMiddleHalf(oldPrint.getPrintSn());
        String dbMaskAfterPrintKey = StringUtils.maskMiddleHalf(oldPrint.getPrintKey());
        String dbMaskAfterThirdKey = StringUtils.maskMiddleHalf(oldPrint.getThirdKey());

        boolean coreChanged = !Objects.equals(dbMaskAfterPrintSn, param.getPrintSn())
                || !Objects.equals(dbMaskAfterPrintKey, param.getPrintKey())
                || !Objects.equals(dbMaskAfterThirdKey, param.getThirdKey());
        if (coreChanged) {
            param.setPrintSn(!Objects.equals(dbMaskAfterPrintSn, param.getPrintSn())
                    ? param.getPrintSn() : oldPrint.getPrintSn());
            param.setPrintKey(!Objects.equals(dbMaskAfterPrintKey, param.getPrintKey())
                    ? param.getPrintKey() : oldPrint.getPrintKey());
            param.setThirdKey(!Objects.equals(dbMaskAfterThirdKey, param.getThirdKey())
                    ? param.getThirdKey() : oldPrint.getThirdKey());
        } else {
            param.setPrintSn(oldPrint.getPrintSn());
            param.setPrintKey(oldPrint.getPrintKey());
            param.setThirdKey(oldPrint.getThirdKey());
        }
        boolean needRebind = coreChanged || !oldPrint.getThirdAccount().equals(param.getThirdAccount());

        if (needRebind) {
            PrintPlatformAdapter adapter = adapterFactory.of(PrintPlatformEnum.SUNMI.getCode());
            // 先尝试解绑旧 SN（失败仅记日志，可能已不存在于商米云）
            UnbindResult ur = adapter.unbind(oldPrint);
            if (ur == null || !ur.isSuccess()) {
                log.warn("商米云解绑原打印机失败，继续尝试绑定新打印机：old_sn={}, msg={}",
                        oldPrint.getPrintSn(), ur == null ? "null" : ur.getErrorMsg());
            }
            // 再绑定新 SN
            Print newPrint = new Print();
            newPrint.setPrintSn(param.getPrintSn());
            newPrint.setPrintKey(param.getPrintKey());
            newPrint.setThirdAccount(param.getThirdAccount());
            newPrint.setThirdKey(param.getThirdKey());
            newPrint.setPrintName(param.getPrintName());
            newPrint.setShopId(getShopId());
            BindResult br = adapter.bind(newPrint);
            if (br == null || !br.isSuccess()) {
                throw new GlobalException("更新打印机失败：" + (br == null ? "调用API失败" : br.getErrorMsg()));
            }
        }

        Print updatePrint = new Print();
        updatePrint.setPrintId(param.getPrintId());
        updatePrint.setPrintName(param.getPrintName());
        updatePrint.setPrintSn(param.getPrintSn());
        updatePrint.setPrintKey(param.getPrintKey());
        updatePrint.setThirdAccount(param.getThirdAccount());
        updatePrint.setThirdKey(param.getThirdKey());
        updatePrint.setPrintNumber(1);
        updatePrint.setUpdateTime(StringUtils.getCurrentTime());
        updatePrint.setStatus(param.getStatus());
        updatePrint.setAutoPrint(param.getAutoPrint());
        if (param.getIsDefault() != null) {
            updatePrint.setIsDefault(Integer.valueOf(1).equals(param.getIsDefault()) ? 1 : 0);
        }
        // 语音开关：前端传值才覆盖，null 保留原值（旧前端兼容）
        if (param.getVoiceEnabled() != null) {
            updatePrint.setVoiceEnabled(param.getVoiceEnabled());
        }
        // 语音内容类型：前端传值才覆盖，null 保留原值
        if (param.getVoiceContentType() != null) {
            updatePrint.setVoiceContentType(param.getVoiceContentType());
        }

        clearOtherDefault(updatePrint.getPrintId(), updatePrint.getIsDefault());
        boolean updated = this.updateById(updatePrint);
        // 纸张尺寸：前端传值时同步到 print_config（旧前端不传则保留历史值）
        if (updated && param.getPaperSize() != null) {
            printConfigService.updatePaperSizeByPrintId(
                    updatePrint.getPrintId(), param.getPaperSize());
        }
        if (updated && param.getDeviceCode() != null) {
            bindingService.replace(getShopId(), updatePrint.getPrintId(), param.getDeviceCode());
        }
        return updated;
    }

    @Override
    public boolean hasEnabled() {
        return this.lambdaQuery()
                .eq(Print::getShopId, getShopId())
                .eq(Print::getStatus, PrintStatusEnum.ENABLED.getCode())
                .exists();
    }

    @Override
    public List<PrintPageVO> devicePrinters(String deviceCode) {
        return routingService.resolve(getShopId(), deviceCode).stream()
                .map(print -> {
                    PrintPageVO vo = new PrintPageVO(print);
                    vo.setDeviceCode(bindingService.bindingsByPrintIds(getShopId(), List.of(print.getPrintId()))
                            .get(print.getPrintId()));
                    return vo;
                }).toList();
    }

    private void clearOtherDefault(Integer currentPrintId, Integer isDefault) {
        if (!Integer.valueOf(1).equals(isDefault)) {
            return;
        }
        lambdaUpdate().set(Print::getIsDefault, 0)
                .eq(Print::getShopId, getShopId())
                .ne(currentPrintId != null, Print::getPrintId, currentPrintId)
                .update();
    }
}




