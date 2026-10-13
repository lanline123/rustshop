package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 门店关联标签
 * @TableName store_tip
 */
@TableName(value ="store_tip")
@Data
public class StoreTip {
    /**
     * 门店标签id
     */
    @TableId(type = IdType.AUTO)
    private Integer storeTipId;

    /**
     * 门店标签id
     */
    private Integer tipId;

    /**
     * 门店id
     */
    private Integer shopId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Long addTime;
}