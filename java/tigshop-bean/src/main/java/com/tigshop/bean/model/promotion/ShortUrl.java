package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

/**
 * @author tigshop
 */
@Data
@TableName("short_url")
@Builder
public class ShortUrl {
    
    /**
     * 主键
     */
    @TableId(value = "short_url_id", type = IdType.AUTO)
    private Integer shortUrlId;
    
    /**
     * 短码，比如 aB12Xz
     */
    @TableField("short_code")
    private String shortCode;
    
    /**
     * 商品详情真实地址
     */
    @TableField("long_url")
    private String longUrl;
    
    /**
     * 生成时间
     */
    @TableField("add_time")
    private String addTime;

    @TableField("type")
    private Integer type;
}
