import zh from "./zh.json";

const LIST_PLACEHOLDER = /\{(\d+)\}/;
const NAMED_PLACEHOLDER = /\{([a-zA-Z_]\w*)\}/;

/**
 * 将含插值占位符（{0}、{name}）的消息字符串改写为 Message Function。
 *
 * vue-i18n 默认的 message compiler 会在运行时通过 `new Function(...)` 把
 * "{0}" / "{name}" 之类的占位符编译成消息函数。但 **微信/支付宝等小程序** 以及
 * **App（APP-PLUS / APP-HARMONY）** 的 JS 沙箱禁用 eval/new Function，
 * 会导致编译失败、原样输出（例如 "已售{0}件"）。
 *
 * 因此统一在构建期把含占位符的字符串改写成 Message Function，绕过运行时编译；
 * 没有占位符的字符串原样下发，对 H5 也无副作用。
 * @see https://ask.dcloud.net.cn/question/174493
 */
export function preprocessMessages(messages: Record<string, string>): Record<string, any> {
    const result: Record<string, any> = {};
    for (const [key, value] of Object.entries(messages)) {
        if (typeof value !== "string" || (!LIST_PLACEHOLDER.test(value) && !NAMED_PLACEHOLDER.test(value))) {
            result[key] = value;
            continue;
        }
        result[key] = (ctx: { list: (i: number) => unknown; named: (k: string) => unknown }) => {
            return value
                .replace(/\{(\d+)\}/g, (m, idx: string) => {
                    const v = ctx.list(Number(idx));
                    return v != null ? String(v) : m;
                })
                .replace(/\{([a-zA-Z_]\w*)\}/g, (m, name: string) => {
                    const v = ctx.named(name);
                    return v != null ? String(v) : m;
                });
        };
    }
    return result;
}

export const localMessages: Record<string, Record<string, string>> = {
    zh,
};

export default {
    zh: preprocessMessages(zh),
};
