// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.print;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 打印任务日志表
 * <p>
 * 记录每次推送打印任务的全生命周期状态、平台、错误原因与原始报文，
 * 用于打印记录列表展示与问题排查。
 *
 * @TableName print_log
 */
@TableName(value = "print_log")
@Data
public class PrintLog {

    /**
     * 自增ID
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 打印机id
     */
    private Integer printId;

    /**
     * 平台 1=飞鹅云 2=商米云
     */
    private Integer platform;

    /**
     * 订单id
     */
    private Integer orderId;

    /**
     * 打印机SN（快照，便于打印机删除后仍能溯源）
     */
    private String printSn;

    /**
     * 触发方式 1=自动 2=手动补打
     */
    private Integer triggerType;

    /**
     * 状态 0=待推送 1=推送成功 2=打印成功 3=推送失败 4=打印机离线
     */
    private Integer status;

    /**
     * 平台错误码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 平台任务号（商米回调用于匹配；飞鹅云可能为空）
     */
    private String taskNo;

    /**
     * 推送API原始响应（截断存储）
     */
    private String pushResponse;

    /**
     * 回调原始报文（仅商米云使用）
     */
    private String callbackPayload;

    /**
     * MQ重试次数
     */
    private Integer retryCount;

    /**
     * 推送时间（Unix 时间戳，秒）
     */
    private Integer pushedTime;

    /**
     * 打印成功时间（Unix 时间戳，秒）
     */
    private Integer printedTime;

    /**
     * 创建时间（Unix 时间戳，秒）
     */
    private Integer addTime;

    /**
     * 更新时间（Unix 时间戳，秒）
     */
    private Integer updateTime;
}
