import { defineConfig } from "vite";
import uni from "@dcloudio/vite-plugin-uni";
// 分包优化：组件异步跨包引用（装修模块下沉到 customPage 分包）
import Optimization from "@uni-ku/bundle-optimizer";

export default defineConfig(({ command }) => {
    const isBuild = command === "build";

    return {
        plugins: [uni(), Optimization()],
        server: {
            port: 3000,
            host: "0.0.0.0",
            proxy: {
                // "": {
                //     target: "", //接口地址
                //     changeOrigin: true,
                //     rewrite: (path) => path.replace(RegExp(`^`), "")
                // }
            }
        },
        build: {
            minify: "terser",
            terserOptions: {
                compress: {
                    drop_console: isBuild,
                    drop_debugger: isBuild
                }
            },
            sourcemap: false
        }
    };
});
