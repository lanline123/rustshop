export type ReasonValidationError = "required" | "empty" | "tooLong" | "duplicate" | "tooMany";

export interface ReasonValidationResult {
    valid: boolean;
    reasons: string[];
    error?: ReasonValidationError;
}

export const normalizeReason = (reason: string): string => String(reason ?? "").trim();

export const validateReasonList = (reasonList: string[], enabled: boolean): ReasonValidationResult => {
    const reasons = Array.isArray(reasonList) ? reasonList.map(normalizeReason) : [];

    if (reasons.length > 10) return { valid: false, reasons, error: "tooMany" };
    if (reasons.some((reason) => !reason)) return { valid: false, reasons, error: "empty" };
    if (reasons.some((reason) => reason.length > 50)) return { valid: false, reasons, error: "tooLong" };
    if (new Set(reasons).size !== reasons.length) return { valid: false, reasons, error: "duplicate" };
    if (enabled && reasons.length === 0) return { valid: false, reasons, error: "required" };

    return { valid: true, reasons };
};
