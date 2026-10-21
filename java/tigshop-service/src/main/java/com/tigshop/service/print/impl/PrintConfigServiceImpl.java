package com.tigshop.service.print.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tigshop.bean.constant.print.PrintTemplateConstant;
import com.tigshop.bean.dto.print.PrintOrderDTO;
import com.tigshop.bean.enums.print.PrintConfigTypeEnum;
import com.tigshop.bean.enums.print.PrintPaperSizeEnum;
import com.tigshop.bean.enums.print.PrintStatusEnum;
import com.tigshop.bean.model.print.Print;
import com.tigshop.bean.model.print.PrintConfig;
import com.tigshop.bean.param.print.PrintConfigUpdateParam;
import com.tigshop.bean.param.print.PrintOrdersParam;
import com.tigshop.bean.vo.print.PrintConfigVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.print.PrintConfigMapper;
import com.tigshop.mapper.print.PrintMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.print.PrintConfigService;
import com.tigshop.service.print.PrintRoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Admin
 * @description 针对表【print_config(打印机配置表)】的数据库操作Service实现
 * @createDate 2025-07-22 09:22:06
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PrintConfigServiceImpl extends BaseServiceImpl<PrintConfigMapper, PrintConfig> implements PrintConfigService {

    private final RabbitTemplate rabbitTemplate;
    private final PrintMapper printMapper;
    private final PrintRoutingService routingService;

    @Override
    public PrintConfigVO getConfigsByPrintId(Integer printId) {
        List<PrintConfig> configs = this.lambdaQuery()
                .eq(PrintConfig::getPrintId, printId)
                .list();
        List<PrintConfigVO> list = configs.stream()
                .map(PrintConfigVO::new)
                .toList();
        return list.stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public boolean updateConfig(PrintConfigUpdateParam param) {
        PrintConfig config = this.getById(param.getId());
        if (config == null) {
            throw new GlobalException("打印配置不存在");
        }

        try {
            // 将PrintTemplate对象转换为JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String templateJson = objectMapper.writeValueAsString(param.getTemplate());
            config.setTemplate(templateJson);
            // 纸张尺寸：前端传值才覆盖，null 保留原值（确保旧前端兼容）
            if (param.getPaperSize() != null) {
                config.setPaperSize(param.getPaperSize());
            }
            config.setUpdateTime(StringUtils.getCurrentTime());
            return this.updateById(config);
        } catch (Exception e) {
            log.error("更新打印配置异常", e);
            throw new GlobalException("更新打印配置失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean initConfig(Integer printId) {
        return initConfig(printId, PrintPaperSizeEnum.SIZE_58);
    }

    @Override
    @Transactional
    public boolean initConfig(Integer printId, PrintPaperSizeEnum paperSize) {
        try {
            // 检查是否已存在配置
            Long count = this.lambdaQuery()
                    .eq(PrintConfig::getPrintId, printId)
                    .count();

            if (count > 0) {
                log.info("打印机ID:{}已存在配置，无需初始化", printId);
                return true;
            }

            // 创建默认购物小票配置
            PrintPaperSizeEnum paper = paperSize == null ? PrintPaperSizeEnum.SIZE_58 : paperSize;
            PrintConfig receiptConfig = new PrintConfig();
            receiptConfig.setPrintId(printId);
            receiptConfig.setType(PrintConfigTypeEnum.RECEIPT.getCode());
            receiptConfig.setTemplate(PrintTemplateConstant.DEFAULT_RECEIPT_TEMPLATE);
            receiptConfig.setPaperSize(paper);
            receiptConfig.setAddTime(StringUtils.getCurrentTime());
            receiptConfig.setUpdateTime(StringUtils.getCurrentTime());
            return this.save(receiptConfig);
        } catch (Exception e) {
            log.error("初始化打印机配置异常", e);
            return false;
        }
    }

    @Override
    public int updatePaperSizeByPrintId(Integer printId, PrintPaperSizeEnum paperSize) {
        if (printId == null || paperSize == null) {
            return 0;
        }
        // MyBatis-Plus 通过 @EnumValue 自动把枚举转 tinyint 存库
        boolean ok = this.lambdaUpdate()
                .eq(PrintConfig::getPrintId, printId)
                .set(PrintConfig::getPaperSize, paperSize)
                .set(PrintConfig::getUpdateTime, StringUtils.getCurrentTime())
                .update();
        return ok ? 1 : 0;
    }

    @Override
    public Boolean print(PrintOrdersParam param) {
        String deviceCode = StrUtil.isBlank(param.getDeviceCode()) ? null : param.getDeviceCode().trim();
        List<Print> printers = routingService.resolve(getShopId(), deviceCode);
        if (printers.isEmpty()) {
            throw new GlobalException(deviceCode == null
                    ? "当前店铺没有启用的打印机"
                    : "所选设备没有绑定启用的打印机");
        }

        for (Integer id : param.getIds()) {
            for (Print print : printers) {
                PrintOrderDTO printOrderDTO = PrintOrderDTO.builder()
                        .print(print)
                        .orderId(id)
                        .deviceCode(deviceCode)
                        .triggerType(com.tigshop.bean.enums.print.PrintTriggerTypeEnum.MANUAL.getCode())
                        .build();
                rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE,
                        RabbitMQConfig.PRINT_ORDER_ROUTING_KEY, printOrderDTO);
            }
        }
        return true;
    }
}




