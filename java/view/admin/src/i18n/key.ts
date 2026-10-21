/**
 * 标记一个字符串为 i18n 翻译 key（仅标记，不翻译）
 *
 * 用于非 Vue 组件上下文（如模块注册、配置对象）中声明需要翻译的文本。
 * 实际翻译在展示层通过 $t() 完成。
 * i18n 脚本通过检测 t() 调用自动收集这些 key 到语言包。
 */
export function t(key: string): string {
    return key;
}
