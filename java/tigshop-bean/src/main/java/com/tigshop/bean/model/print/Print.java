package com.tigshop.bean.model.print;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 打印机表
 * @TableName print
 */
@TableName(value ="print")
@Data
public class Print {
    /**
     * 自增ID，打印机ID
     */
    @TableId(type = IdType.AUTO)
    private Integer printId;

    /**
     * 打印机名称
     */
    private String printName;

    /**
     * 打印机SN
     */
    private String printSn;

    /**
     * 打印机Key
     */
    private String printKey;

    /**
     * 第三方平台对接账号
     */
    private String thirdAccount;

    /**
     * 第三方平台key
     */
    private String thirdKey;

    /**
     * 打印联数
     */
    private Integer printNumber;

    /**
     * 第三放平台 1飞鹅云 
     */
    private Integer platform;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 状态 1开启 2关闭
     */
    private Integer status;

    /**
     * 订单支付自动打印 1开启 2关闭
     */
    private Integer autoPrint;

    /**
     * 语音播报内容类型：0=订单号+金额，1=仅金额，null 兜底为 0（历史兼容）
     */
    private Integer voiceContentType;

    /** 店铺默认打印机：1 是，0 否。历史数据为空时按非默认处理。 */
    private Integer isDefault;

    /**
     * 语音播报开关：1=开启，2=关闭，null 兜底为 1（历史兼容）
     * <p>
     * <b>平台差异</b>：
     * <ul>
     *     <li>商米云：控制 {@code pushContent.media_text + cycle=1} 是否下发</li>
     *     <li>飞鹅云：控制 {@code <PLUGIN>} 标签是否拼入 content 开头（触发外接音响/钱箱）</li>
     * </ul>
     */
    private Integer voiceEnabled;

    /**
     * 打印机创建时间
     */
    private Long addTime;

    /**
     * 打印机修改时间
     */
    private Long updateTime;

    /**
     * 打印机删除时间
     */
    private Long deleteTime;
}
