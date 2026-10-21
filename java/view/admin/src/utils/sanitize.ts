/**
 * 将不可信字符串进行 HTML 转义，避免在 v-html/innerHTML 等场景触发 XSS。
 *
 * 注意：
 * - Vue 模板插值（{{ text }}）默认会转义，一般不需要再手动 escape。
 * - 只有在确实要输出 HTML（如 v-html）时，才应对不可信输入做 escape 或白名单净化。
 */
export function escapeHtml(input: unknown): string {
    const text = input == null ? "" : String(input);
    const map: Record<string, string> = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    };
    return text.replace(/[&<>"']/g, (m) => map[m] ?? m);
}


