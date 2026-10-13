package com.tigshop.service.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.StockWarningBatchSaveDTO;
import com.tigshop.bean.dto.product.StockWarningExportDTO;
import com.tigshop.bean.dto.product.StockWarningListDTO;
import com.tigshop.bean.dto.product.StockWarningSaveDTO;
import com.tigshop.bean.dto.product.StockWarningSettingSaveDTO;
import com.tigshop.bean.vo.product.StockWarningBatchResultVO;
import com.tigshop.bean.vo.product.StockWarningExcelVO;
import com.tigshop.bean.vo.product.StockWarningImportResultVO;
import com.tigshop.bean.vo.product.StockWarningPageVO;
import com.tigshop.bean.vo.product.StockWarningSettingVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 库存预警服务
 *
 * @author Tigshop团队
 */
public interface StockWarningService {

    /**
     * 获取指定主体的库存预警通知总开关
     *
     * @param shopId 归属主体：0=平台自营，>0=店铺或门店
     * @return 开关配置
     */
    StockWarningSettingVO getSetting(Integer shopId);

    /**
     * 保存指定主体的库存预警通知总开关
     *
     * @param saveDTO 开关参数
     */
    void saveSetting(StockWarningSettingSaveDTO saveDTO);

    /**
     * 库存预警列表分页
     *
     * @param listDTO 查询条件
     * @return 分页结果
     */
    Page<StockWarningPageVO> list(StockWarningListDTO listDTO);

    /**
     * 导出前端勾选的商品/SKU 行；不沿用页面筛选条件，导出结果与勾选项一一对应
     *
     * @param exportDTO 导出参数，ids 为必填的勾选行标识
     * @return 导出行
     */
    List<StockWarningExcelVO> export(StockWarningExportDTO exportDTO);

    /**
     * 下载导入模板，模板列与导入解析共用同一份定义
     *
     * @param response 响应，写出 xlsx 二进制流
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入预警规则。按编码定位行：多规格填规格编码，单规格填商品编码；
     * 导入的规则一律置为开启，合法行照常入库，非法行汇总在失败明细中返回。
     *
     * @param file   上传的 xlsx 文件
     * @param shopId 归属主体：0=平台自营，>0=店铺或门店
     * @return 成功数量与失败明细
     */
    StockWarningImportResultVO importExcel(MultipartFile file, Integer shopId);

    /**
     * 根据原导入文件导出失败行，供导入失败后的“下载失败数据”使用。
     *
     * @param file     原始 xlsx 文件
     * @param shopId   归属主体
     * @param response 响应，写出失败数据 xlsx
     */
    void getImportErrorData(MultipartFile file, Integer shopId, HttpServletResponse response);

    /**
     * 保存单个商品/SKU 的预警规则
     *
     * @param saveDTO 规则参数
     */
    void save(StockWarningSaveDTO saveDTO);

    /**
     * 批量保存预警规则，单项失败不影响其他项
     *
     * @param batchSaveDTO 批量参数
     * @return 成功数量与失败明细
     */
    StockWarningBatchResultVO batchSave(StockWarningBatchSaveDTO batchSaveDTO);

    /**
     * 库存变动后重新判定预警并按需发送站内消息。
     * 同一预警状态不重复发送；库存为 0 时不在此发送消息，无货消息沿用下单链路的既有逻辑。
     * 判定失败不会影响调用方的主业务流程。
     *
     * @param shopId    归属主体：0=平台自营，>0=店铺或门店
     * @param productId 商品ID
     * @param skuId     SKU ID；null 或 0 表示单规格商品
     */
    void evaluate(Integer shopId, Integer productId, Integer skuId);
}
