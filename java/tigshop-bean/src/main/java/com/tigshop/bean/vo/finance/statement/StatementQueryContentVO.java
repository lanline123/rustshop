package com.tigshop.bean.vo.finance.statement;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 查询内容数据
 *
 * @author Tigshop项目组
 * @create 2025年08月13日 14:07
 */
@Data
public class StatementQueryContentVO {
    private List<Map<String, Object>> statementType;
    private List<Map<String, Object>> statementTimeType;
    private List<Map<String, Object>> accountType;
    private List<Map<String, Object>> entryType;
    private List<Map<String, Object>> payMethodType;
    private List<Map<String, Object>> dateComponentType;
}