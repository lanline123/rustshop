import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { defineConfig } from "vitest/config";

const projectRoot = dirname(fileURLToPath(import.meta.url));

export default defineConfig(async () => {
    const vue = (await import("@vitejs/plugin-vue")).default;

    return {
        plugins: [
            vue(),
            {
                name: "strip-pages-jsonc-comments",
                enforce: "pre" as const,
                transform(code: string, id: string) {
                    if (id.endsWith("src/pages.json")) {
                        return { code: code.replace(/^\s*\/\/.*$/gm, ""), map: null };
                    }
                }
            }
        ],
        test: {
            environment: "jsdom",
            globals: true,
            include: ["src/**/*.{test,spec}.{js,ts}"],
            exclude: ["node_modules", "dist", "unpackage"],
            testTimeout: 10000,
            hookTimeout: 10000
        },
        resolve: {
            alias: {
                "@": resolve(projectRoot, "src")
            }
        }
    };
});
