import js from "@eslint/js";
import tseslint from "typescript-eslint";
import pluginVue from "eslint-plugin-vue";
import prettier from "eslint-config-prettier";
import pluginPrettier from "eslint-plugin-prettier";
import globals from "globals";

export default [
    // 基础配置
    js.configs.recommended,

    // TypeScript 配置
    ...tseslint.configs.recommended,

    // Vue 配置
    ...pluginVue.configs["flat/recommended"],

    // Prettier 集成
    prettier,

    {
        languageOptions: {
            globals: {
                ...globals.browser,
                ...globals.node,
                // uni-app 全局变量和 API
                uni: "readonly",
                plus: "readonly",
                wx: "readonly",
                getCurrentPages: "readonly",
                getApp: "readonly",
                getOpenerEventChannel: "readonly",
                __wxConfig: "readonly",

                // uni-app 可能作为函数的情况
                uniCloud: "readonly",
                requirePlugin: "readonly",

                // 微信小程序全局变量
                App: "readonly",
                Page: "readonly",
                Component: "readonly",
                Behavior: "readonly",
                getRegExp: "readonly",

                // 其他可能的全局变量
                my: "readonly", // 支付宝小程序
                swan: "readonly", // 百度小程序
                tt: "readonly", // 字节跳动小程序
                qq: "readonly", // QQ 小程序
                jd: "readonly" // 京东小程序
            },
            ecmaVersion: "latest",
            sourceType: "module",
            parserOptions: {
                parser: tseslint.parser
            }
        },
        plugins: {
            prettier: pluginPrettier
        },
        rules: {
            "prettier/prettier": [
                "error",
                {},
                {
                    usePrettierrc: true,
                    fileInfoOptions: {
                        withNodeModules: true
                    }
                }
            ],
            "@typescript-eslint/no-explicit-any": "off",
            "@typescript-eslint/no-unused-vars": ["warn", { argsIgnorePattern: "^_" }],
            "no-restricted-imports": [
                "error",
                {
                    paths: [
                        {
                            name: "@/hooks",
                            importNames: ["useProductStyle", "useCouponInfoMap"],
                            message: "为避免跨 chunk 循环依赖，请从具体 hook 模块直接导入。"
                        },
                        {
                            name: "@/hooks/index",
                            importNames: ["useProductStyle", "useCouponInfoMap"],
                            message: "为避免跨 chunk 循环依赖，请从具体 hook 模块直接导入。"
                        },
                        {
                            name: "@/utils/request",
                            importNames: ["handleLogin"],
                            message: "登录处理属于上层业务，请从 @/utils/handleLogin 直接导入，避免 request 核心反向依赖业务模块。"
                        },
                        {
                            name: "@/utils",
                            importNames: ["isShowB2bPriceText"],
                            message: "为避免 utils 桶反向依赖 Store，请从 @/utils/b2bPrice 直接导入。"
                        },
                        {
                            name: "@/utils/index",
                            importNames: ["isShowB2bPriceText"],
                            message: "为避免 utils 桶反向依赖 Store，请从 @/utils/b2bPrice 直接导入。"
                        }
                    ]
                }
            ],
            "vue/multi-word-component-names": "off",
            "vue/no-unused-vars": "warn",
            "vue/html-self-closing": [
                "error",
                {
                    html: {
                        void: "always",
                        normal: "always",
                        component: "always"
                    },
                    svg: "always",
                    math: "always"
                }
            ]
        }
    },

    // 忽略配置
    {
        ignores: ["node_modules/", "dist/", "unpackage/", "*.min.js", ".vscode/", ".idea/", "harmony-configs/", "coverage/", "**/*.v1.vue"]
    },

    // Vue 文件特定配置
    {
        files: ["**/*.vue"],
        languageOptions: {
            parserOptions: {
                parser: tseslint.parser,
                extraFileExtensions: [".vue"]
            }
        }
    },

    // TypeScript 文件特定配置
    {
        files: ["**/*.ts", "**/*.tsx"],
        languageOptions: {
            parser: tseslint.parser
        }
    },

    // utils 桶入口不得反向依赖 request，否则会与 request 对 utils 的依赖形成源码环
    {
        files: ["src/utils/index.ts"],
        rules: {
            "no-restricted-imports": [
                "error",
                {
                    paths: [
                        {
                            name: "@/utils/request",
                            message: "请从无业务反向依赖的叶子模块导入，避免 utils/request 循环依赖。"
                        },
                        {
                            name: "./request",
                            message: "请从无业务反向依赖的叶子模块导入，避免 utils/request 循环依赖。"
                        }
                    ],
                    patterns: [
                        {
                            group: ["@/utils/request.*", "@/utils/request/**", "./request.*", "./request/**"],
                            message: "请从无业务反向依赖的叶子模块导入，避免 utils/request 循环依赖。"
                        },
                        {
                            group: ["@/store", "@/store/**"],
                            message: "utils 桶不得反向导入 Store；请将对应能力拆到调用方直引的叶子模块。"
                        }
                    ]
                }
            ]
        }
    },

    // request 核心只能依赖叶子模块；Store、启动器和 utils 桶均会经 API 反向回到 request
    {
        files: ["src/utils/request.ts"],
        rules: {
            "no-restricted-imports": [
                "error",
                {
                    paths: [
                        {
                            name: "@/utils",
                            message: "请改为具体叶子模块直引，避免 utils 桶经 Store/API 反向回到 request。"
                        },
                        {
                            name: "@/utils/index",
                            message: "请改为具体叶子模块直引，避免 utils 桶经 Store/API 反向回到 request。"
                        },
                        {
                            name: "@/utils/init",
                            message: "请通过 requestRuntime 注入初始化能力，避免 request 与启动链互相导入。"
                        },
                        {
                            name: "@/utils/handleLogin",
                            message: "请通过 requestRuntime 注入登录处理，保持 request 核心无业务反向依赖。"
                        }
                    ],
                    patterns: [
                        {
                            group: ["@/store", "@/store/**"],
                            message: "请通过 requestRuntime 获取运行时状态，避免 Store/API/request 循环依赖。"
                        }
                    ]
                }
            ]
        }
    },

    // Config Store 不得导入内部反向读取 Config/Region Store 的复合格式化模块
    {
        files: ["src/store/config.ts"],
        rules: {
            "no-restricted-imports": [
                "error",
                {
                    paths: [
                        {
                            name: "@/utils/format",
                            importNames: ["imageFormat"],
                            message: "请使用无 Store 依赖的 @/utils/imageUrl，避免 config/format 循环依赖。"
                        }
                    ]
                }
            ]
        }
    }
];
