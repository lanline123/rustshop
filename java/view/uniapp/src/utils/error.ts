/**
 * 将 catch 块中的 unknown 错误转为可展示文案
 */
export function getErrorMessage(error: unknown, fallback = ""): string {
    if (error instanceof Error) return error.message;
    if (typeof error === "string") return error;
    if (typeof error === "object" && error !== null && "message" in error) {
        const message = (error as { message: unknown }).message;
        if (typeof message === "string") return message;
    }
    return fallback;
}
