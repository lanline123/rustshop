/// <reference types="vite/client" />
declare module "pinia-plugin-persist";
declare module "@gausszhou/vue3-drag-resize-rotate";

declare module "*.vue" {
    import type { DefineComponent } from "vue";
    const component: DefineComponent<{}, {}, any>;
    export default component;
}