export const FeeType = {
    FIXED: 0,
    PERCENT: 1
} as const;

export type FeeTypeValue = (typeof FeeType)[keyof typeof FeeType];
