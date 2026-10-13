<template>
    <div class="demo-collapse">
        <el-collapse v-model="activeNames">
            <draggable item-key="" :list="<any>comboGroups" ghost-class="ghost" chosen-class="chosenClass" animation="300" @start="" @end="">
                <template #item="{ element, index }">
                    <div class="collapse-item">
                        <el-collapse-item :name="index + 1">
                            <template #title>{{ $t('第【{index1}】组', { index1: index + 1 }) }}</template>

                            <div class="dec-edit-group">
                                <div style="width: 100%">
                                    <div class="tips">{{ $t('图片: 宽20, 高20') }}</div>
                                    <PicList :isMultiple="true" :showDesc="true" v-model:photos="element.notices" :title="$t('添加公告')" decorateType="mobile"></PicList>
                                </div>
                            </div>
                        </el-collapse-item>
                    </div>
                </template>
            </draggable>
        </el-collapse>
    </div>
</template>
<script lang="ts" setup>
import draggable from "vuedraggable";
import { PicSelect, PicList } from "@/components/decorate";
import { ref } from "vue";
import { SelectLink } from "@/components/select";
import { ComboGroupNoticeType } from "@/types/decorate/decorate.d";
const comboGroups = defineModel<ComboGroupNoticeType[]>("modelValue", { default: () => [] });
const props = defineProps({
    title: {
        type: String,
        default: ""
    },
    activeNames: {
        type: Array,
        default: () => [1]
    }
});
const activeNames = ref(props.activeNames);
</script>
<style scoped lang="less">
.demo-collapse {
    padding: 0 10px 15px 10px;
    :deep(.el-collapse) {
        border: none;
    }
    .collapse-item {
        width: 100%;
        position: relative;
        margin-top: 12px;
        border-radius: 2px;
        background-color: #fff;
        box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.2);
        :deep(.el-collapse-item__header) {
            background-color: #f7f8fa !important;
            padding-left: 10px;
        }
        .del-item {
            position: absolute;
            cursor: pointer;
            right: -10px;
            top: -10px;
            color: #fff;
            background: #bbb;
            border-radius: 50%;
            z-index: 2;
            width: 18px;
            height: 18px;
            text-align: center;
            line-height: 18px;
            display: none;
            .ico-decorate {
                font-size: 12px;
            }
        }
    }
    .collapse-item:hover .del-item {
        display: block;
    }
    .tips {
        color: var(--tig-primary);
    }
    .cobot-btn .dec-pic-add-btn {
        box-sizing: border-box;
        position: relative;
        display: -ms-flexbox;
        display: flex;
        -ms-flex-pack: center;
        justify-content: center;
        -ms-flex-align: center;
        align-items: center;
        margin-top: 12px;
        padding: 9px 16px;
        border: 1px solid var(--tig-primary);
        border-radius: 2px;
        background: #fff;
        color: var(--tig-primary);
        font-size: 14px;
        line-height: 20px;
        cursor: pointer;
    }
}
</style>
