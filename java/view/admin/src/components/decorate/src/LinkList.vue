<template>
    <div style="width:100%">
        <div class="gallery-list-warp">
            <div class="dec-pic-group">
                <div class="dec-pic-group-list">
                    <draggable class="gallery-list-ul" item-key="" :list="links" ghost-class="ghost" chosen-class="chosenClass" animation="300" @start="" @end="">
                        <template #item="{ element, index }">
                            <div class="dec-pic-group-item">
                                <div class="dec-pic-group-item-con">
                                    <div class="item-info">
                                        <div class="item-info-item item-info-title">
                                            <span class="lable">{{ $t('标题') }}</span>
                                            <el-input :placeholder="$t('建议10个字以内，可不填')" v-model="element.linkTitle"></el-input>
                                        </div>
                                        <div class="item-info-item item-info-link">
                                            <span class="lable">{{ $t('链接') }}</span>
                                            <div class="lyecs-link-select">
                                                <SelectLink v-model="element.link"></SelectLink>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="del-item" @click="removePic(index)"><i class="ico-decorate icon-dec-cha"></i></div>
                                </div>
                            </div>
                        </template>
                    </draggable>
                </div>
                <div class="dec-pic-add-con">
                    <a class="dec-pic-add-btn" @click="onAdd"><i class="ico-decorate icon-dec-jia"></i>{{ $t('添加链接') }}</a>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { reactive, ref, toRefs } from "vue"
import type { Ref } from "vue"
import { DialogForm } from '@/components/dialog'
import draggable from "vuedraggable";
import { Image } from '@/components/image';
import { SelectLink } from '@/components/select';
import { imageFormat } from "@/utils/format";
const dom: Ref<HTMLDivElement> = ref(null) as any

const props = defineProps({
    modelValue: {
        type: Array,
        default: []
    },
})
// 动态解析props
const links = ref(props.modelValue)

const onEdit = (result, data) => {
    Object.assign(links.value[data.index], result[0])
}
const onAdd = () => {
    links.value.push({
        linkTitle: '',
        link: {
            link: '',
            title: '',
        }
    })
}
const removePic = (index) => {
    links.value.splice(<any>index, 1)
}
</script>

<style lang="less" scoped></style>
