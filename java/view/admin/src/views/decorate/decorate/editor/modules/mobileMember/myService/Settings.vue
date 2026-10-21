<template>
    <perfect-scrollbar class="decorate-edit-con">
        <div class="dec-edit-title">
            <h3>我的服务</h3>
            <div class="dec-edit-desc">支持列表版和九宫格版两种布局</div>
        </div>
        <Tabs :tabs="tabs" v-model="activeName"></Tabs>
        <div v-if="activeName === 'content'">
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">模块标题</div>
                </div>
                <div class="dec-edit-group-con">
                    <TigInput v-model="module.moduleTitle" placeholder="我的服务" :maxlength="10" />
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">布局样式</div>
                </div>
                <div class="dec-edit-group-con">
                    <el-radio-group class="dec-radio-group" v-model="module.layoutStyle">
                        <el-radio-button :value="1">列表版</el-radio-button>
                        <el-radio-button :value="2">九宫格</el-radio-button>
                    </el-radio-group>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-title">
                    <div class="label">图标圆角</div>
                </div>
                <div class="dec-edit-group-con">
                    <div class="dec-range-group">
                        <el-slider v-model="module.iconRadius" show-input :show-input-controls="false" size="small" input-size="default" :max="50" />
                    </div>
                </div>
            </div>
            <div class="dec-divider-line"></div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">默认服务</div>
                </div>
                <div class="dec-edit-group-con mb10">
                    <a-spin :spinning="loading">
                        <div class="default-service-list">
                            <el-tag
                                size="large"
                                :disable-transitions="true"
                                v-for="(value, key) in menus"
                                :key="key"
                                class="mx-1"
                                type="info"
                                @click="addTag(value)"
                            >
                                {{ value.picTitle }}
                            </el-tag>
                        </div>
                    </a-spin>
                </div>
            </div>
            <div class="dec-edit-group dec-edit-group-block">
                <div class="dec-edit-group-title">
                    <div class="label">服务列表</div>
                </div>
            </div>
            <div class="dec-edit-group">
                <div class="dec-edit-group-con">
                    <PicList v-model:photos="module.serviceList" :isMultiple="true" :showText="true" decorateType="mobile" title="添加服务项"></PicList>
                </div>
            </div>
        </div>
        <div v-if="activeName === 'extend'">
            <CommonFrameEdit v-model="module.frame"></CommonFrameEdit>
        </div>
    </perfect-scrollbar>
</template>
<script lang="ts" setup>
import { ref, onMounted, computed } from "vue";
import { message } from "ant-design-vue";
import Tabs from "@/components/tabs/Index.vue";
import CommonFrameEdit from "@/views/decorate/decorate/src/modules/src/commonFrame/Edit.vue";
import { PicList } from "@/components/decorate";
import { mergeDefaultModule, defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { getMenberDecorateData } from "@/api/decorate/decorateDiscrete";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import type { MyServiceModule } from "./types";

const store = useDecorateEditorStore();
const module = computed(() => (store.activeModuleData ?? {}) as MyServiceModule);

const tabs = ref<any[]>([
    { label: "内容设置", name: "content" },
    { label: "扩展设置", name: "extend" }
]);
const activeName = ref("content");

mergeDefaultModule(module.value, {
    moduleTitle: "我的服务",
    layoutStyle: 1,
    iconRadius: 50,
    serviceList: [],
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 10,
        innerPadding: 15,
        boxPadding: 10,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
});

const menus = ref<any[]>([]);
const loading = ref(true);

const addTag = (item: any) => {
    module.value.serviceList.push({ ...item });
};

const _getMenberDecorateData = async () => {
    try {
        const result = await getMenberDecorateData();
        const list = Array.isArray(result.menus) ? result.menus : Object.values(result.menus || {});
        menus.value = list;
        if (!module.value.serviceList?.length) {
            module.value.serviceList = list.map((item: any) => ({ ...item }));
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    _getMenberDecorateData();
});
</script>
<style lang="less" scoped>
.default-service-list {
    display: flex;
    flex-wrap: wrap;
}
.mx-1 {
    margin: 5px;
    cursor: pointer;
}
.dec-edit-group-con {
    :deep(.el-tag) {
        border: none !important;
        background-color: #f2f2f2 !important;
        color: #666 !important;
    }
}
</style>
