export type RefundRejectReasonError = "required" | "tooLong";

export const isRefundReadOnlyStatus = (status: number | string | undefined) => [2, 3, 4].includes(Number(status));

export const validateRefundRejectReason = (reason: string): RefundRejectReasonError | null => {
    const normalizedReason = String(reason ?? "").trim();
    if (!normalizedReason) return "required";
    if (normalizedReason.length > 255) return "tooLong";
    return null;
};

export const buildRefundRejectPayload = (refundId: number, reason: string) => ({
    refundId,
    refundStatus: 4,
    refundNote: reason.trim()
});
