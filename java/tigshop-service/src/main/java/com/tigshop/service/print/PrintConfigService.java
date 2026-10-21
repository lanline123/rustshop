package com.tigshop.service.print;

import com.tigshop.bean.enums.print.PrintPaperSizeEnum;
import com.tigshop.bean.model.print.PrintConfig;
import com.tigshop.bean.param.print.PrintConfigUpdateParam;
import com.tigshop.bean.param.print.PrintOrdersParam;
import com.tigshop.bean.vo.print.PrintConfigVO;
import com.tigshop.service.common.BaseService;
import jakarta.validation.Valid;

/**
* @author Admin
* @description 针对表【print_config(打印机配置表)】的数据库操作Service
* @createDate 2025-07-22 09:22:06
*/
public interface PrintConfigService extends BaseService<PrintConfig> {

    /**
     * 获取打印机配置列表
     *
     * @param printId 打印机ID
     * @return 配置列表
     */
    PrintConfigVO getConfigsByPrintId(Integer printId);

    /**
     * 更新打印机配置
     *
     * @param param 配置参数
     * @return 更新结果
     */
    boolean updateConfig(PrintConfigUpdateParam param);

    /**
     * 初始化打印机配置（默认 58mm）
     *
     * @param printId 打印机ID
     * @return 初始化结果
     */
    boolean initConfig(Integer printId);

    /**
     * 初始化打印机配置（指定纸张枚举）
     *
     * @param printId   打印机ID
     * @param paperSize 纸张尺寸枚举，{@code null} 兜底为 58mm
     * @return 初始化结果
     */
    boolean initConfig(Integer printId, PrintPaperSizeEnum paperSize);

    /**
     * 更新该打印机下所有配置的纸张尺寸（后台"编辑打印机"时调用，保证同机多模板纸张一致）
     *
     * @param printId   打印机ID
     * @param paperSize 新纸张尺寸枚举，{@code null} 视为不变更
     * @return &gt;0 表示成功
     */
    int updatePaperSizeByPrintId(Integer printId, PrintPaperSizeEnum paperSize);

    /**
     * 打印订单
     *
     * @param param 打印参数
     * @return 打印结果
     */
    Boolean print(@Valid PrintOrdersParam param);
}
