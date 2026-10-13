/** 将经营许可证开关值规范化为契约允许的 0/1，缺失或非法值默认开启。 */
export const normalizeBusinessLicenseEnabled = (value: unknown): 0 | 1 => (value === 0 || value === "0" ? 0 : 1);
