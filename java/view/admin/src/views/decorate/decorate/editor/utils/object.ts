/**
 * 装修编辑器对象工具（移植自 Vort-Admin utils/common/object）
 */

/**
 * 深度合并默认值与来源数据：
 * - 嵌套普通对象递归合并（数组按值整体覆盖）
 * - source 中 undefined 的字段保留默认值
 * 返回新对象，不修改入参（区别于旧编辑器 mergeDefaultModule 的原地烧录）
 */
export function deepMerge<T extends object>(defaults: T, source: Partial<T>): T {
    const result = { ...defaults } as T;

    for (const key in source) {
        if (Object.prototype.hasOwnProperty.call(source, key)) {
            const sourceValue = source[key as keyof T];
            const defaultValue = defaults[key as keyof T];

            if (
                sourceValue !== null &&
                typeof sourceValue === "object" &&
                !Array.isArray(sourceValue) &&
                defaultValue !== null &&
                typeof defaultValue === "object" &&
                !Array.isArray(defaultValue)
            ) {
                (result as Record<string, unknown>)[key] = deepMerge(defaultValue as object, sourceValue as object);
            } else if (sourceValue !== undefined) {
                (result as Record<string, unknown>)[key] = sourceValue;
            }
        }
    }

    return result;
}
