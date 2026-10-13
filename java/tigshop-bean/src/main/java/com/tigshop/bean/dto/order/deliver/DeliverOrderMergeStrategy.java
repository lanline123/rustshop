package com.tigshop.bean.dto.order.deliver;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.tigshop.bean.vo.order.OrderDeliverImportErrorExcelVO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 批量发货Excel合并策略
 * 按订单号合并订单属性、会员信息、发货参数列，商品相关列不合并
 */
public class DeliverOrderMergeStrategy extends AbstractMergeStrategy implements CellWriteHandler, SheetWriteHandler {

    private final Map<Integer, Integer> rowSpanMap = new LinkedHashMap<>();
    private boolean isMerged = false;
    private final int[] mergeCols;
    private CellStyle centerStyle;
    // 第一行说明文字内容
    private final String noticeText;
    // 第一行说明文字需要合并的列数
    private final int noticeColSpan;
    private boolean noticeWritten = false;

    /**
     * 需要合并的列索引（用于OrderDeliverExcelDTO）
     * 0: 订单编号, 1: 会员名称, 2: 收货人, 3: 手机号, 4: 收货地址, 5: 订单金额
     * 10: 配送方式, 11: 物流公司, 12: 物流代号, 13: 物流单号, 14: 下单时间
     */
    private static final int[] DELIVER_EXCEL_MERGE_COLS = {0, 1, 2, 3, 4, 5, 10, 11, 12, 13, 14};

    /**
     * 需要合并的列索引（用于OrderDeliverImportErrorExcelVO）
     * 0: 行号, 1: 订单编号
     * 5: 配送方式, 6: 物流公司, 7: 物流单号, 8: 错误信息
     */
    private static final int[] ERROR_EXCEL_MERGE_COLS = {0, 1, 5, 6, 7, 8};

    /**
     * 构造方法
     * @param dataList 导出的数据列表
     * @param orderSnGetter 获取订单号的函数
     * @param mergeCols 需要合并的列索引
     */
    public <T> DeliverOrderMergeStrategy(List<T> dataList, Function<T, String> orderSnGetter, int[] mergeCols) {
        this(dataList, orderSnGetter, mergeCols, null, 0, 1);
    }

    /**
     * 构造方法（支持说明行）
     * @param dataList 导出的数据列表
     * @param orderSnGetter 获取订单号的函数
     * @param mergeCols 需要合并的列索引
     * @param noticeText 说明文字（null则不添加说明行）
     * @param noticeColSpan 说明行合并的列数
     * @param dataStartRow 数据起始行号（0-based，考虑说明行后表头在第1行，数据在第2行时传2）
     */
    public <T> DeliverOrderMergeStrategy(List<T> dataList, Function<T, String> orderSnGetter, int[] mergeCols,
                                         String noticeText, int noticeColSpan, int dataStartRow) {
        this.mergeCols = mergeCols;
        this.noticeText = noticeText;
        this.noticeColSpan = noticeColSpan;
        // 构建：每个订单从第几行开始、合并多少行
        Map<String, Long> countMap = dataList.stream()
                .collect(Collectors.groupingBy(orderSnGetter, LinkedHashMap::new, Collectors.counting()));

        // 数据从dataStartRow行开始
        int startRow = dataStartRow;
        for (Map.Entry<String, Long> entry : countMap.entrySet()) {
            rowSpanMap.put(startRow, entry.getValue().intValue());
            startRow += entry.getValue();
        }
    }

    /**
     * 便捷构造方法（用于OrderDeliverExcelDTO，带说明行）
     */
    public static DeliverOrderMergeStrategy forOrderDeliverExcelWithNotice(List<OrderDeliverExcelDTO> dataList,
                                                                          String noticeText, int noticeColSpan) {
        // 说明行在第0行，表头在第1行，数据从第2行开始
        return new DeliverOrderMergeStrategy(dataList, OrderDeliverExcelDTO::getOrderSn, DELIVER_EXCEL_MERGE_COLS,
                noticeText, noticeColSpan, 2);
    }

    /**
     * 便捷构造方法（用于OrderDeliverExcelDTO）
     */
    public static DeliverOrderMergeStrategy forOrderDeliverExcel(List<OrderDeliverExcelDTO> dataList) {
        return new DeliverOrderMergeStrategy(dataList, OrderDeliverExcelDTO::getOrderSn, DELIVER_EXCEL_MERGE_COLS);
    }

    /**
     * 便捷构造方法（用于OrderDeliverImportErrorExcelVO）
     */
    public static DeliverOrderMergeStrategy forErrorExcel(List<OrderDeliverImportErrorExcelVO> dataList) {
        return new DeliverOrderMergeStrategy(dataList, OrderDeliverImportErrorExcelVO::getOrderSn, ERROR_EXCEL_MERGE_COLS);
    }

    /**
     * Sheet创建后写入说明行
     */
    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        if (noticeText == null || noticeWritten) {
            return;
        }
        
        Sheet sheet = writeSheetHolder.getSheet();
        Workbook workbook = sheet.getWorkbook();
        
        // 创建第一行说明行
        Row noticeRow = sheet.createRow(0);
        Cell noticeCell = noticeRow.createCell(0);
        noticeCell.setCellValue(noticeText);
        
        // 创建红色字体样式
        CellStyle noticeStyle = workbook.createCellStyle();
        Font redFont = workbook.createFont();
        redFont.setColor(IndexedColors.RED.getIndex());
        redFont.setBold(true);
        noticeStyle.setFont(redFont);
        noticeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        noticeCell.setCellStyle(noticeStyle);
        
        // 合并第一行的单元格
        if (noticeColSpan > 1) {
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, noticeColSpan - 1));
        }
        
        noticeWritten = true;
    }

    /**
     * 单元格写入后设置居中样式
     */
    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head,
                                 Integer relativeRowIndex, Boolean isHead) {
        if (Boolean.TRUE.equals(isHead)) {
            return;
        }

        // 初始化居中样式
        if (centerStyle == null) {
            Workbook workbook = writeSheetHolder.getSheet().getWorkbook();
            centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);
            centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            centerStyle.setWrapText(true); // 自动换行
        }

        // 设置所有单元格居中
        cell.setCellStyle(centerStyle);
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        if (isMerged) return;

        for (Map.Entry<Integer, Integer> entry : rowSpanMap.entrySet()) {
            int firstRow = entry.getKey();
            int span = entry.getValue();
            int lastRow = firstRow + span - 1;

            // 跳过只占1行的，不合并
            if (span < 2) continue;

            for (int col : mergeCols) {
                sheet.addMergedRegionUnsafe(new CellRangeAddress(firstRow, lastRow, col, col));
            }
        }

        isMerged = true;
    }
}
