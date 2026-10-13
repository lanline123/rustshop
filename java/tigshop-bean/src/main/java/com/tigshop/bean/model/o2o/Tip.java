package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 门店标签
 * @TableName tip
 */
@TableName(value ="tip")
@Data
public class Tip {
    /**
     * 门店标签id
     */
    @TableId(type = IdType.AUTO)
    private Integer tipId;

    /**
     * 门店标签名称 
     */
    private String tipName;

    /**
     * 0 禁用，1 启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Long addTime;
}