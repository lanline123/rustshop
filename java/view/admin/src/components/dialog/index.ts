
export { default as DialogForm } from './src/DialogForm.vue';
import { createApp, ref, unref } from "vue";
import FormModal from "./src/FormModal.vue";
import { regGlobalComponets } from '@/components/regGlobal';
//options{path:[路径参数，如:brand/Info],params:[传递给页面的参数，如:{id:1}]}
const dialog = Object.assign((options: any) => {  //用Object.assign是避免后面dialog.show()这些方法语法报错
    // 创建元素节点
    const rootNode = document.createElement("div");
    rootNode.setAttribute('type', 'dialog')
    document.body.appendChild(rootNode);
    const app = createApp(options.modal, {
        ...options,   //传递给根组件的 props，path必填
        afterClose() {
            // 卸载已挂载的应用实例
            app.unmount();
            // 删除rootNode节点
            document.body.removeChild(rootNode);
        },
    });
    regGlobalComponets(app);
    return app.mount(rootNode);
});
dialog.install = (app: any) => {
    // app.config.globalProperties.$dialog = (options: any) => dialog(options).show();
};
// 定义show方法用于直接调用
dialog.show = (options: any) => {
    dialog(Object.assign({ modal: FormModal }, options)).show();
}

dialog.dradwer = (options: any) => {
    dialog(Object.assign({ modal: FormModal, isDrawer: true }, options)).show();
}

dialog.gallery = (options: any) => {
    dialog(Object.assign({ modal: FormModal, title: '相册', path: 'gallery/Gallery', class: 'lyecs-modal-gallery', width: '811px' }, options)).show();
}



export default dialog;