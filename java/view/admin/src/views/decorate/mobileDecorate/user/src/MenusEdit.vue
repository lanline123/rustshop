<template>
    <div class="dec-spread-title">
        <div class="title">{{ $t('我的服务设置') }}</div>
    </div>
    <div class="dec-edit-group dec-edit-group-block">
        <div class="dec-edit-group-title">
            <div class="title">{{ $t('选择风格') }}</div>
        </div>
        <div class="dec-edit-group-con mb20">
            <el-radio-group class="dec-radio-group" v-model="module.menuType" style="width: 300px">
                <el-radio-button :value="1">{{ $t('列表版') }}</el-radio-button>
                <el-radio-button :value="2">{{ $t('九宫格版') }}</el-radio-button>
            </el-radio-group>
        </div>
        <div class="dec-edit-group-title">
            <div class="title">{{ $t('默认模块') }}</div>
        </div>
        <div class="dec-edit-group-con mb10">
            <a-spin :spinning="loading">
                <div class="list">
                    <el-tag
                        size="large"
                        :disable-transitions="true"
                        v-for="(value, key) in menus"
                        :key="key"
                        class="mx-1"
                        type="info"
                        @click="addTag(value, key)"
                    >
                        {{ value.picTitle }}
                    </el-tag>
                </div>
            </a-spin>
        </div>
        <div class="dec-edit-group-title">
            <div class="title">{{ $t('添加模块') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <PicList :isMultiple="true" v-model:photos="module.menuList" :title="$t('添加模块')" decorateType="mobile"></PicList>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/views/decorate/decorate/src/css/decorate.less";
import { ref, onMounted } from "vue";
import { PicList } from "@/components/decorate";
import { getMenberDecorateData } from "@/api/decorate/decorateDiscrete";
import { DecorateUserMenu } from "@/types/decorate/decorateDiscrete.d";
import { message } from "ant-design-vue";
const module = defineModel<DecorateUserMenu>("module", { type: Object as () => DecorateUserMenu, default: { menuType: 1, menuList: [] } });
const loading = ref(true);
const addTag = (item: any, index: number) => {
    module.value.menuList.push(item);
};
onMounted(() => {
    _getMenberDecorateData();
});
const menus = ref<any>([]);
const _getMenberDecorateData = async () => {
    try {
        const result = await getMenberDecorateData();
        menus.value = result.menus;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
</script>
<style lang="less" scoped>
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
