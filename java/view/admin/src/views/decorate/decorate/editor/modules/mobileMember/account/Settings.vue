<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>账号设置</h3>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">模块标题</div>
                </div>
                <div class="dec-edit-group-con">
                    <TigInput v-model="module.moduleTitle" placeholder="请输入标题" :maxlength="10" />
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">账号列表设置</div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-con">
                    <PicList
                        v-model:photos="module.accountList"
                        :isMultiple="true"
                        :showText="true"
                        :showLink="false"
                        :showAddButton="false"
                        title="添加账号项"
                    ></PicList>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref, computed } from "vue";
import Tabs from "@/components/tabs/Index.vue";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { PicList } from "@/components/decorate";
import { ModulePicListType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { isOverseas } from "@/utils/version";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { AccountModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as AccountModule);

const tabs = ref<any[]>([
    { label: "内容设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");

const defaultAccountList: ModulePicListType[] = [
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779691120KR8ldT5BHs0ZKZ4Uxk.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779691120KR8ldT5BHs0ZKZ4Uxk.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "账号详情",
        picDesc: ""
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779691120QiTfocEWPiXAr7ILcG.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779691120QiTfocEWPiXAr7ILcG.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "地址",
        picDesc: ""
    },
    {
        picUrl: "https://oss.tigshop.com/img/gallery/202605/1779691120penAIAi6kRrFMWeeoe.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picThumb: "https://oss.tigshop.com/img/gallery/202605/1779691120penAIAi6kRrFMWeeoe.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picTitle: "设置",
        picDesc: ""
    }
];

mergeDefaultModule(module.value, {
    moduleTitle: "账号",
    accountList: defaultAccountList.map((item) => ({ ...item })),
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 0,
        innerPadding: 15,
        boxPadding: 0,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
});
</script>

<style lang="scss" scoped>
:deep(.dec-pic-group) {
    .item-info {
        display: flex;
        flex-direction: column;
        justify-content: center;
    }
}
</style>
