// 首字母转大写
export function capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1);
}