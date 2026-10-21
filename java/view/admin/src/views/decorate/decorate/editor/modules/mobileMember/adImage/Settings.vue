<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>广告图</h3>
            <div class="dec-edit-desc">单张广告图片展示</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">广告图片</div>
                </div>
                <div class="dec-edit-group-con">
                    <PicSelect v-model:photo="module.pic" :default-value="defaultPic.picUrl"></PicSelect>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图片链接</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="lyecs-link-select">
                        <SelectLink v-model="module.picLink"></SelectLink>
                    </div>
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
import { PicSelect } from "@/components/decorate";
import { SelectLink } from "@/components/select";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { AdImageModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as AdImageModule);

const tabs = ref<any[]>([
    { label: "内容设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");

const defaultPic = {
    picUrl: "https://oss.tigshop.com/img/gallery/202605/1779689214Nbzv6Iw3ldEVFucDjc.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
    picThumb: "https://oss.tigshop.com/img/gallery/202605/1779689214Nbzv6Iw3ldEVFucDjc.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200"
};

mergeDefaultModule(module.value, {
    pic: { ...defaultPic },
    picLink: { link: "", name: "" },
    frame: {
        ...defaultFrame,
        backgroundColor: "",
        boxRadius: 12,
        innerPadding: 0,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
});
</script>
