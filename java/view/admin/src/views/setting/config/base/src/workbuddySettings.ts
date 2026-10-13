export function maskWorkBuddyKey(value: string | null | undefined): string {
    if (!value) return "";
    if (value.length <= 8) return "*".repeat(value.length);
    return `${value.slice(0, 3)}${"*".repeat(Math.max(4, value.length - 7))}${value.slice(-4)}`;
}

export function validateWorkBuddyExpiresAt(value: string | null | undefined, now = Date.now()): boolean {
    if (!value) return false;
    const timestamp = Date.parse(value);
    return Number.isFinite(timestamp) && timestamp > now;
}

/** 空值或无法解析的时间返回 undefined，避免把 NaN 序列化成 null 交给后端。 */
export function toWorkBuddyExpiresAt(value: string | Date | null | undefined): number | undefined {
    if (!value) return undefined;
    const timestamp = new Date(value).getTime();
    return Number.isFinite(timestamp) ? Math.floor(timestamp / 1000) : undefined;
}

export function fromWorkBuddyExpiresAt(value: number | null | undefined): string {
    if (!value) return "";
    return new Date(value * 1000).toISOString().replace(/\.\d{3}Z$/, "Z");
}

/** 远程 MCP 配置示例只使用占位符，避免把真实环境参数带入页面或剪贴板。 */
const MCP_CONNECTION_TEMPLATE = `{
  "mcpServers": {
    "tigshop-mcp": {
      "url": "http://你的Tigshop域名/adminapi/mcp",
      "headers": {
        "X-MCP-Api-Key": "你的MCP密钥"
      },
      "disabled": false
    }
  }
}`;

export function getMcpConnectionTemplate(): string {
    return MCP_CONNECTION_TEMPLATE;
}

/** 在 HTTPS 与 HTTP 后台环境中均尝试复制文本。 */
export async function copyWorkBuddyText(text: string): Promise<void> {
    if (typeof navigator !== "undefined" && navigator.clipboard && typeof window !== "undefined" && window.isSecureContext) {
        await navigator.clipboard.writeText(text);
        return;
    }
    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.style.position = "fixed";
    textarea.style.opacity = "0";
    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();
    const copied = document.execCommand("copy");
    document.body.removeChild(textarea);
    if (!copied) throw new Error("浏览器拒绝访问剪贴板");
}
