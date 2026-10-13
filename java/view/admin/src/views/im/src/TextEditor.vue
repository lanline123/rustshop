<template>
    <div style="width: 100%" :style="border ? 'border: 1px solid #eee' : ''">
        <Toolbar style="border-bottom: 1px solid #eee" :editor="editorRef" :defaultConfig="toolbarConfig" :mode="mode" />
        <Editor :style="'height: ' + height + '; overflow-y: hidden;'" @onChange="onChange" v-model="html" :defaultConfig="editorConfig" :mode="mode" @onCreated="handleCreated" />
    </div>
    <DialogForm type="gallery" ref="galleryDialog" class="item-bg" @okCallback="onEdit" :params="{ isMultiple: true }">
    </DialogForm>
</template>
<script setup lang="ts">
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import { ref, onMounted, onBeforeUnmount, shallowRef, toRefs } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { DialogForm } from '@/components/dialog'
import { imageFormat } from "@/utils/format";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    html: String,
    height: {
        type: String,
        default: '500px'
    },
    border: {
        type: Boolean,
        default: true
    }
})
const html = ref(props.html)
const galleryDialog = shallowRef()
const emit = defineEmits(['update:html'])
// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef()
// 内容 HTML
const mode = 'default'
onMounted(() => {
    emit('update:html', html)
})
const fn = ref();
const toolbarConfig = ref({
    toolbarKeys: ['emotion'], // 只显示表情按钮
});

const editorConfig = {
    placeholder: t('请输入内容...'),
    MENU_CONF: {
        uploadImage: {
            // 自定义选择图片
            customBrowseAndUpload(insertFn:any) {
                galleryDialog.value.show()
                fn.value = insertFn;
            }
        }
    }

}
const onChange = (editor:any) => {
}
const onEdit = (result:any) => {
    for (let idx in result) {
        fn.value(imageFormat(result[idx].picUrl), '', '')
    }

}
// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
    const editor = editorRef.value
    if (editor == null) return
    editor.destroy()
})

const handleCreated = (editor:any) => {
    editorRef.value = editor // 记录 editor 实例，重要！
}


</script>
