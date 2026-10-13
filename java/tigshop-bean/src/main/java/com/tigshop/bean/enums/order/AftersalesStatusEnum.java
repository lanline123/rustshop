// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.order;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import java.util.List;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;

/**
 * @author Tigshop
 */
@Getter
public enum AftersalesStatusEnum {
    /**
     * 审核处理中
     */
    IN_REVIEW(1, "审核处理中"),
    /**
     * 审核通过
     */
    APPROVED_FOR_PROCESSING(2, "审核通过"),
    /**
     * 审核未通过
     */
    REFUSE(3, "审核未通过"),
    /**
     * 待用户回寄
     */
    SEND_BACK(4, "待回寄"),
    /**
     * 待商家收货
     */
    RETURNED(5, "待商家收货"),
    /**
     * 已完成
     */
    COMPLETE(6, "已完成"),
    /**
     * 已取消
     */
    CANCEL(7, "已取消"),
    /**
     * 待供应商审核
     */
    WAIT_FOR_SUPPLIER_AUDIT(21, "待供应商审核"),
    /**
     * 待供应商收货
     */
    WAIT_FOR_SUPPLIER_RECEIPT(22, "待供应商收货");

    private final int code;
    private final String description;

    AftersalesStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 判断一个值是否为有效的 code
     *
     * @param code 状态码
     * @return 如果 code 有效则返回 true；否则返回 false
     */
    public static boolean isValidCode(int code) {
        for (AftersalesStatusEnum status : values()) {
            if (status.code == code) {
                return true;
            }
        }
        return false;
    }

    public static String getStatusName(Integer status) {
        for (AftersalesStatusEnum aftersalesStatusEnum : values()) {
            if (aftersalesStatusEnum.code == status) {
                return aftersalesStatusEnum.getDescription();
            }
        }
        return "";
    }

    /**
     * 售后状态副文案（详情状态卡副行）。无对应文案时返回 null，前端按 status 本地兜底。
     */
    public static String getStatusTip(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case 1 -> "买家已提交售后申请，请尽快审核处理";
            case 2 -> "售后申请已通过，等待后续处理";
            case 3 -> "售后申请已被拒绝，可查看拒绝原因";
            case 4 -> "已同意退货，等待买家寄回商品";
            case 5 -> "买家已寄回商品，请确认收货或拒收";
            case 6 -> "售后已完成";
            case 7 -> "售后已取消";
            case 21 -> "等待供应商审核售后申请";
            case 22 -> "买家已寄回商品，等待供应商确认收货或拒收";
            default -> null;
        };
    }

    /** closeType：商家审核驳回 */
    public static final int CLOSE_TYPE_AUDIT_REJECT = 1;
    /** closeType：商家/供应商拒收 */
    public static final int CLOSE_TYPE_REJECT_RECEIVE = 2;

    private static boolean isSupplier(Integer vendorId) {
        return vendorId != null && vendorId > 0;
    }

    private static boolean isPayReturn(Integer aftersaleType) {
        return aftersaleType != null && aftersaleType == AftersalesTypeEnum.PAYRETURN.getCode();
    }

    private static String withReason(String prefix, String reply) {
        return prefix + (reply == null ? "" : reply);
    }

    /**
     * 后台售后详情状态头名称（按 aftersaleType/status/vendorId/是否财务驳回 区分）。
     * 与 {@link #getStatusName} 不同，后者用于列表/日志等其他场景，保持原文案。
     */
    public static String getAdminStatusName(Integer aftersaleType, Integer status,
                                            Integer vendorId, boolean financeRejected) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case 1 -> isSupplier(vendorId) ? "店铺审核中" : "买家申请售后";
            case 21 -> "供应商审核中";
            case 2 -> isPayReturn(aftersaleType)
                    ? (financeRejected ? "平台已拒绝" : "平台处理中")
                    : "已收到退货，退款处理中";
            case 4 -> "售后已通过，待用户回寄";
            case 5 -> "已退货，待商家收货";
            case 22 -> "已退货，待供应商收货";
            case 6 -> "售后已完成";
            case 3, 7 -> "售后已关闭";
            default -> null;
        };
    }

    /**
     * 后台售后详情状态副文案。带倒计时的状态（1/21）返回 null，由 autoAgreeEndTime 驱动倒计时展示。
     * status=3 的关闭原因由 closeType 区分（审核驳回 / 拒收）；理由拼接 reply。
     */
    public static String getAdminStatusDescription(Integer aftersaleType, Integer status,
                                                    Integer vendorId, Integer closeType,
                                                    String reply, boolean financeRejected) {
        if (status == null) {
            return null;
        }
        boolean supplier = isSupplier(vendorId);
        return switch (status) {
            case 1, 21 -> null;
            case 2 -> isPayReturn(aftersaleType)
                    ? (financeRejected ? withReason("退款申请已被拒绝，理由：", reply) : "售后已通过，平台退款处理中")
                    : "退货申请已通过，平台退款处理中";
            case 4 -> "售后已同意，请及时通知买家填写退货物流";
            case 5, 22 -> "买家已寄出商品，请及时确认收货";
            case 6 -> "退款已成功，售后流程结束";
            case 3 -> {
                if (isPayReturn(aftersaleType)) {
                    yield withReason("退款申请已被拒绝，理由：", reply);
                }
                if (closeType != null && closeType == CLOSE_TYPE_REJECT_RECEIVE) {
                    yield supplier
                            ? withReason("供应商已拒收商品，理由：", reply) + "，售后终止"
                            : withReason("商家已拒收商品，理由：", reply) + "，售后终止";
                }
                yield supplier
                        ? withReason("供应商退货退款申请已被拒绝，理由：", reply)
                        : withReason("退货退款申请已被拒绝，理由：", reply);
            }
            default -> null;
        };
    }

    /**
     * 前台买家售后详情状态名称。买家不区分供应商（21→审核，22→待商家收货）。
     */
    public static String getClientStatusName(Integer aftersaleType, Integer status,
                                             Integer closeType, boolean financeRejected) {
        if (status == null) {
            return null;
        }
        boolean payReturn = isPayReturn(aftersaleType);
        return switch (status) {
            case 1, 21 -> payReturn ? "商家审核" : "申请退货退款";
            case 2 -> payReturn
                    ? (financeRejected ? "退款失败（平台财务）" : "财务退款")
                    : "商家已收货，退款中";
            case 4 -> "待退货";
            case 5, 22 -> "已退货，待商家收货";
            case 6 -> "退款成功";
            case 3 -> {
                if (payReturn) {
                    yield "退款拒绝(商家)";
                }
                yield (closeType != null && closeType == CLOSE_TYPE_REJECT_RECEIVE)
                        ? "商家拒收" : "退货退款被拒绝";
            }
            case 7 -> "已取消";
            default -> null;
        };
    }

    /**
     * 前台买家售后详情状态副文案。带倒计时的状态（1/21）文案不含具体时间，倒计时由 autoAgreeEndTime 驱动。
     */
    public static String getClientStatusDesc(Integer aftersaleType, Integer status,
                                             Integer closeType, String reply, boolean financeRejected) {
        if (status == null) {
            return null;
        }
        boolean payReturn = isPayReturn(aftersaleType);
        return switch (status) {
            case 1, 21 -> payReturn
                    ? "你的退款申请已提交，等待商家审核，超时未处理将自动同意。"
                    : "你的退货退款申请已提交，等待商家审核，超时未处理将自动同意。";
            case 2 -> {
                if (payReturn) {
                    yield financeRejected
                            ? withReason("您的退款申请已被财务拒绝，拒绝理由：", reply) + "，可联系商家协商或重新申请。"
                            : "商家已同意退款，财务正在处理退款，将按原路退回。";
                }
                yield "商家已签收商品，财务正在处理退款，将按原路退回。";
            }
            case 4 -> "申请已通过，请填写退货物流信息并寄回商品。";
            case 5, 22 -> "商品已寄出，等待商家签收，签收后将为你处理退款。";
            case 6 -> payReturn
                    ? "退款已成功到账，售后流程已结束。"
                    : "退款已成功到账，退货退款流程已结束。";
            case 3 -> {
                if (payReturn) {
                    yield withReason("你的退款申请已被商家拒绝，拒绝理由：", reply) + "，可联系商家协商或重新申请。";
                }
                yield (closeType != null && closeType == CLOSE_TYPE_REJECT_RECEIVE)
                        ? withReason("商家已拒收退回的商品，拒绝理由：", reply) + "，售后已终止，可联系商家协商。"
                        : withReason("你的退货退款申请已被商家拒绝，拒绝理由：", reply) + "，可联系商家协商或重新申请。";
            }
            default -> null;
        };
    }

    /**
     * 前台买家售后详情状态名称的多语言文案 key（与 {@link #getClientStatusName} 分支一一对应），
     * 由调用方通过 MessageHelper 按请求语言解析。
     */
    public static String getClientStatusNameKey(Integer aftersaleType, Integer status,
                                                Integer closeType, boolean financeRejected) {
        if (status == null) {
            return null;
        }
        boolean payReturn = isPayReturn(aftersaleType);
        return switch (status) {
            case 1, 21 -> payReturn ? "aftersales.client.status.review.refund" : "aftersales.client.status.review.return";
            case 2 -> payReturn
                    ? (financeRejected ? "aftersales.client.status.finance.rejected" : "aftersales.client.status.finance.refunding")
                    : "aftersales.client.status.received.refunding";
            case 4 -> "aftersales.client.status.wait.return";
            case 5, 22 -> "aftersales.client.status.returned.wait.receipt";
            case 6 -> "aftersales.client.status.refund.success";
            case 3 -> {
                if (payReturn) {
                    yield "aftersales.client.status.refund.rejected";
                }
                yield (closeType != null && closeType == CLOSE_TYPE_REJECT_RECEIVE)
                        ? "aftersales.client.status.receive.rejected" : "aftersales.client.status.return.rejected";
            }
            case 7 -> "aftersales.client.status.cancelled";
            default -> null;
        };
    }

    /**
     * 前台买家售后详情状态副文案的多语言文案 key（与 {@link #getClientStatusDesc} 分支一一对应）。
     * 含拒绝理由的文案使用 {0} 占位，由调用方传入 reply。
     */
    public static String getClientStatusDescKey(Integer aftersaleType, Integer status,
                                                Integer closeType, boolean financeRejected) {
        if (status == null) {
            return null;
        }
        boolean payReturn = isPayReturn(aftersaleType);
        return switch (status) {
            case 1, 21 -> payReturn ? "aftersales.client.desc.review.refund" : "aftersales.client.desc.review.return";
            case 2 -> {
                if (payReturn) {
                    yield financeRejected ? "aftersales.client.desc.finance.rejected" : "aftersales.client.desc.finance.refunding";
                }
                yield "aftersales.client.desc.received.refunding";
            }
            case 4 -> "aftersales.client.desc.wait.return";
            case 5, 22 -> "aftersales.client.desc.returned.wait.receipt";
            case 6 -> payReturn ? "aftersales.client.desc.refund.success" : "aftersales.client.desc.return.refund.success";
            case 3 -> {
                if (payReturn) {
                    yield "aftersales.client.desc.refund.rejected";
                }
                yield (closeType != null && closeType == CLOSE_TYPE_REJECT_RECEIVE)
                        ? "aftersales.client.desc.receive.rejected" : "aftersales.client.desc.return.rejected";
            }
            default -> null;
        };
    }

    public static AftersalesStatusEnum getStatus(Integer status) {
        for (AftersalesStatusEnum aftersalesStatusEnum : values()) {
            if (aftersalesStatusEnum.code == status) {
                return aftersalesStatusEnum;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }

    /**
     * 代表该售后有效的状态。
     * 审核未通过（REFUSE）为终态，不占用订单商品的可售后数量，故不计入。
     *
     * @return
     */
    public static List<Integer> validStatus() {
        return List.of(
                IN_REVIEW.getCode(),
                APPROVED_FOR_PROCESSING.getCode(),
                SEND_BACK.getCode(),
                RETURNED.getCode(),
                COMPLETE.getCode());
    }

    /**
     * 售后终态：已完成、已取消、审核未通过。
     * 用于「订单是否在售后中」的反向判定，避免各处各自罗列而口径分叉。
     *
     * @return
     */
    public static List<Integer> terminalStatus() {
        return List.of(
                COMPLETE.getCode(),
                CANCEL.getCode(),
                REFUSE.getCode());
    }

    /**
     * 售后进行中的状态：去除 已完成、已取消 和 审核未通过（三者均为终态）
     *
     * @return
     */
    public static List<Integer> inProgressStatus() {
        return List.of(
                IN_REVIEW.getCode(),
                APPROVED_FOR_PROCESSING.getCode(),
                SEND_BACK.getCode(),
                RETURNED.getCode());
    }
}
