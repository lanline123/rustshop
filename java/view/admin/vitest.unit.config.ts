/**
 * 纯 TS 单元测试配置（装修编辑器地基层等）
 *
 * 不复用 vitest.config.ts：其 mergeConfig(viteConfig) 写法与 vite.config.ts
 * 的回调导出不兼容（存量问题），且纯 TS 测试无需加载完整 vite 插件链。
 * 用法：npx vitest run --config vitest.unit.config.ts
 */
import { fileURLToPath } from "node:url";
import { defineConfig } from "vitest/config";

export default defineConfig({
    resolve: {
        alias: {
            "@": fileURLToPath(new URL("./src", import.meta.url))
        }
    },
    test: {
        environment: "node",
        include: ["src/**/__tests__/**/*.test.ts"]
    }
});
