<template>
    <div class="demo-collapse">
        <draggable :list="<any>groups" item-key="title" ghost-class="ghost" chosen-class="chosenClass" animation="300">
            <template #item="{ element, index }">
                <div class="collapse-item">
                    <div class="title-box">{{ $t('第【{index1}】组', { index1: index + 1 }) }}</div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('标题') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-input-group">
                                <el-input v-model="element.title" :placeholder="$t('请输入标题内容')"></el-input>
                            </div>
                        </div>
                    </div>
                    <div class="dec-edit-group">
                        <div class="dec-edit-group-title">
                            <div class="label">{{ $t('链接') }}</div>
                        </div>
                        <div class="dec-edit-group-con">
                            <div class="dec-link-group">
                                <div class="lyecs-link-select">
                                    <SelectLink v-model="element.link" decorateType="mobile"></SelectLink>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div v-if="groups.length > 1" class="del-item" @click="removeItem(index)"><i class="ico-decorate icon-dec-cha"></i></div>
                </div>
            </template>
        </draggable>
        <div class="cobot-btn">
            <a @click="onAdd" class="dec-pic-add-btn"><i class="ico-decorate icon-dec-jia"></i>{{ title }}</a>
        </div>
    </div>
</template>
<script lang="ts" setup>
import draggable from "vuedraggable";
import { SelectLink } from "@/components/select";
import { ModuleTagNavGroupType } from "@/types/decorate/decorate.d";
const groups = defineModel<ModuleTagNavGroupType[]>("modelValue", { default: () => [] });
defineProps({
    title: {
        type: String,
        default: "添加分组"
    }
});
const onAdd = () => {
    const newGroup: ModuleTagNavGroupType = {
        title: "",
        link: { link: { link: "", title: "" }, linkTitle: "" }
    };
    groups.value.push(newGroup);
};
const removeItem = (index: number) => {
    groups.value.splice(index, 1);
};
</script>
<style scoped lang="less">
.demo-collapse {
    padding: 0 10px 15px 10px;
    .collapse-item {
        width: 100%;
        position: relative;
        margin-top: 12px;
        border-radius: 2px;
        background-color: #fff;
        box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.2);
        .title-box {
            padding: 15px;
            font-size: 14px;
            border-bottom: 1px solid #f0f0f0;
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
    .cobot-btn .dec-pic-add-btn {
        box-sizing: border-box;
        position: relative;
        display: flex;
        justify-content: center;
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
