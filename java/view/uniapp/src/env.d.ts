/// <reference types="vite/client" />

declare module "*.vue" {
    import { DefineComponent } from "vue";
    // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/ban-types
    const component: DefineComponent<{}, {}, any>;
    export default component;
}
declare module "uview-plus" {
    global {
        interface Uni {
            $u: any;
        }
    }
}
declare module "crypto-js/aes";
declare module "crypto-js/enc-utf8";
declare module "crypto-js/mode-ecb";
declare module "crypto-js/pad-pkcs7";
