import { fileURLToPath, URL } from "node:url";
import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import vueJsx from "@vitejs/plugin-vue-jsx";
import path from "path";
import { viteMockServe } from "vite-plugin-mock";
import { createSvgIconsPlugin } from "vite-plugin-svg-icons";
import { version } from "./package.json";
export default defineConfig(({ command, mode }) => {
    // 获取.env文件里定义的环境变量
    const env = loadEnv(mode, process.cwd());
    //.env文件中的环境变量必须以VITE_为前缀，否则无法引用成功
    const { VITE_BASE_URL, VITE_REQUEST_URL_PREFIX, VITE_BASE_DIR, VITE_NODE_ENV, VITE_PROXY_TARGET } = env;
    const apiProxyTarget = VITE_PROXY_TARGET || VITE_BASE_URL;

    // const getBasePath = () => {
    //     if (VITE_BASE_DIR) {
    //         return VITE_BASE_DIR.endsWith("/") ? VITE_BASE_DIR : VITE_BASE_DIR + "/";
    //     }
    //     return VITE_NODE_ENV === "production" ? "./" : "/";
    // };

    return {
        build: {
            outDir: "admin-dist",
            emptyOutDir: true,
            sourcemap: false
        },
        plugins: [
            vue({
                script: {
                    defineModel: true
                }
            }),
            vueJsx(),
            // Mock
            viteMockServe({
                supportTs: false,
                logger: false,
                mockPath: "./src/mock/"
            }),
            createSvgIconsPlugin({
                iconDirs: [path.resolve(process.cwd(), "src/assets/icons")],
                symbolId: "icon-[dir]-[name]"
            })
        ],
        base: (VITE_BASE_DIR ?? "") + "/",
        // base: getBasePath(),
        server: {
            host: "0.0.0.0",
            open: true,
            ...(command === "serve" && apiProxyTarget && VITE_REQUEST_URL_PREFIX
                ? {
                      proxy: {
                          [VITE_REQUEST_URL_PREFIX]: {
                              target: apiProxyTarget,
                              changeOrigin: true
                          },
                          // IM 接口使用独立 prefix `/im`，需与 adminapi 一并代理
                          "/im": {
                              target: apiProxyTarget,
                              changeOrigin: true
                          }
                      }
                  }
                : {})
        },
        resolve: {
            alias: {
                "@": path.resolve(__dirname, "./src") // 用 @ 符号替换 src 文件路径
            }
        },
        define: {
            __APP_VERSION__: JSON.stringify(version)
        }
    };
});
