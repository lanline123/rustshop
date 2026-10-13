package com.tigshop.bean.vo.salesman;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderMergeStrategy extends AbstractMergeStrategy {

    private final List<CustomerTransactionOrderExportVO> dataList;
    private final Map<Integer, Integer> rowSpanMap = new LinkedHashMap<>();
    private boolean isMerged = false;

    /**
     * @param dataList 导出的数据列表（每条商品一行）
     */
    public OrderMergeStrategy(List<CustomerTransactionOrderExportVO> dataList) {
        this.dataList = dataList;

        // 构建：每个订单从第几行开始、合并多少行
        Map<String, Long> countMap = dataList.stream()
                .collect(Collectors.groupingBy(CustomerTransactionOrderExportVO::getOrderSn, LinkedHashMap::new, Collectors.counting()));

        // 数据从第1行开始（跳过表头）
        int startRow = 1;
        for (Map.Entry<String, Long> entry : countMap.entrySet()) {
            rowSpanMap.put(startRow, entry.getValue().intValue());
            startRow += entry.getValue();
        }
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        if (isMerged) return;

        // 指定要合并的列索引：订单编号（第0列）、下单时间（第1列）
        int[] mergeCols = {0, 1, 3, 4, 5, 6, 7, 8, 9, 10};

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

