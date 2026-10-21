package com.tigshop.bean.model.print;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.bean.enums.print.PrintPaperSizeEnum;
import lombok.Data;

/**
 * 打印机配置表
 * @TableName print_config
 */
@TableName(value ="print_config")
@Data
public class PrintConfig {
    /**
     * 自增ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 打印机ID
     */
    private Integer printId;

    /**
     * 打印模版JSON
     */
    private String template;

    /**
     * 类型 1购物小票 
     */
    private Integer type;

    /**
     * 纸张尺寸。DB 列为 tinyint（58 / 80），MyBatis-Plus 通过 {@code @EnumValue} 自动
     * 与 {@link PrintPaperSizeEnum} 互转；老数据 null / 0 在 {@code of(Integer)} 兜底为 58mm。
     */
    private PrintPaperSizeEnum paperSize;

    /**
     * 创建时间
     */
    private Long addTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}