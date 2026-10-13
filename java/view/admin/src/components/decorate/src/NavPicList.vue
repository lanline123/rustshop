<template>
    <div style="width: 100%">
        <div class="gallery-list-warp" v-if="photos.length > 0">
            <div class="dec-pic-group">
                <div class="dec-pic-group-list">
                    <div class="dec-pic-group-item">
                        <div class="dec-pic-group-item-con">
                            <div class="item-image">
                                <div class="item-image-con">
                                    <img class="item-image-src" v-if="photos[0].picActiveThumb" :src="imageFormat(photos[0].picActiveThumb)" />
                                    <DialogForm type="gallery" class="" @okCallback="onEditActive" :params="{ isMultiple: false }" :data="{ index: 0 }">
                                        <span class="change-image">{{ $t('更换图片') }}</span>
                                    </DialogForm>
                                    <div class="item-text">{{ $t('选中') }}</div>
                                </div>
                                <div class="item-image-con">
                                    <img class="item-image-src" v-if="photos[0].picThumb" :src="imageFormat(photos[0].picThumb)" />
                                    <DialogForm type="gallery" class="" @okCallback="onEdit" :params="{ isMultiple: false }" :data="{ index: 0 }">
                                        <span class="change-image">{{ $t('更换图片') }}</span>
                                    </DialogForm>
                                    <div class="item-text">{{ $t('未选中') }}</div>
                                </div>
                            </div>
                            <div class="item-info">
                                <div class="item-info-item item-info-title">
                                    <span class="lable">{{ $t('标题') }}</span>
                                    <TigInput width="100%" :placeholder="$t('建议10个字以内，可不填')" v-model="photos[0].picTitle"></TigInput>
                                </div>
                                <div class="item-info-item item-info-link">
                                    <span class="lable">{{ $t('链接') }}</span>
                                    <div class="lyecs-link-select">
                                        <SelectLink v-model="photos[0].picLink" disabled :decorateType="decorateType"></SelectLink>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <draggable
                        class="gallery-list-ul"
                        item-key=""
                        :list="<any>photos"
                        ghost-class="ghost"
                        chosen-class="chosenClass"
                        animation="300"
                        @start=""
                        @end=""
                    >
                        <template #item="{ element, index }">
                            <div class="dec-pic-group-item" v-if="index !== 0 && index !== photos.length - 1">
                                <div class="dec-pic-group-item-con">
                                    <div class="item-image">
                                        <div class="item-image-con">
                                            <img class="item-image-src" v-if="element.picActiveThumb" :src="imageFormat(element.picActiveThumb)" />
                                            <DialogForm
                                                type="gallery"
                                                class=""
                                                @okCallback="onEditActive"
                                                :params="{ isMultiple: false }"
                                                :data="{ index: index }"
                                            >
                                                <span class="change-image">{{ $t('更换图片') }}</span>
                                            </DialogForm>
                                            <div class="item-text">{{ $t('选中') }}</div>
                                        </div>
                                        <div class="item-image-con">
                                            <img class="item-image-src" v-if="element.picThumb" :src="imageFormat(element.picThumb)" />
                                            <DialogForm type="gallery" class="" @okCallback="onEdit" :params="{ isMultiple: false }" :data="{ index: index }">
                                                <span class="change-image">{{ $t('更换图片') }}</span>
                                            </DialogForm>
                                            <div class="item-text">{{ $t('未选中') }}</div>
                                        </div>
                                    </div>
                                    <div class="item-info">
                                        <div class="item-info-item item-info-title">
                                            <span class="lable">{{ $t('标题') }}</span>
                                            <TigInput width="100%" :placeholder="$t('建议10个字以内，可不填')" v-model="element.picTitle"></TigInput>
                                        </div>
                                        <div class="item-info-item item-info-link">
                                            <span class="lable">{{ $t('链接') }}</span>
                                            <div class="lyecs-link-select">
                                                <SelectLink v-model="element.picLink" :disabled="index == 0" :decorateType="decorateType"></SelectLink>
                                            </div>
                                        </div>
                                    </div>
                                    <div v-if="isMultiple == true" class="del-item" @click="removePic(index)"><i class="ico-decorate icon-dec-cha"></i></div>
                                </div>
                            </div>
                        </template>
                    </draggable>
                    <div class="dec-pic-group-item">
                        <div class="dec-pic-group-item-con">
                            <div class="item-image">
                                <div class="item-image-con">
                                    <img
                                        class="item-image-src"
                                        v-if="photos[photos.length - 1].picActiveThumb"
                                        :src="imageFormat(photos[photos.length - 1].picActiveThumb)"
                                    />
                                    <DialogForm
                                        type="gallery"
                                        class=""
                                        @okCallback="onEditActive"
                                        :params="{ isMultiple: false }"
                                        :data="{ index: photos.length - 1 }"
                                    >
                                        <span class="change-image">{{ $t('更换图片') }}</span>
                                    </DialogForm>
                                    <div class="item-text">{{ $t('选中') }}</div>
                                </div>
                                <div class="item-image-con">
                                    <img
                                        class="item-image-src"
                                        v-if="photos[photos.length - 1].picThumb"
                                        :src="imageFormat(photos[photos.length - 1].picThumb)"
                                    />
                                    <DialogForm
                                        type="gallery"
                                        class=""
                                        @okCallback="onEdit"
                                        :params="{ isMultiple: false }"
                                        :data="{ index: photos.length - 1 }"
                                    >
                                        <span class="change-image">{{ $t('更换图片') }}</span>
                                    </DialogForm>
                                    <div class="item-text">{{ $t('未选中') }}</div>
                                </div>
                            </div>
                            <div class="item-info">
                                <div class="item-info-item item-info-title">
                                    <span class="lable">{{ $t('标题') }}</span>
                                    <TigInput width="100%" :placeholder="$t('建议10个字以内，可不填')" v-model="photos[photos.length - 1].picTitle"></TigInput>
                                </div>
                                <div class="item-info-item item-info-link">
                                    <span class="lable">{{ $t('链接') }}</span>
                                    <div class="lyecs-link-select">
                                        <SelectLink v-model="photos[photos.length - 1].picLink" disabled :decorateType="decorateType"></SelectLink>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="dec-pic-add-con" v-if="photos.length < 5">
                    <a class="dec-pic-add-btn" @click="onAdd"><i class="ico-decorate icon-dec-jia"></i>{{ $t('添加导航{length}/5', { length: photos.length }) }}</a>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { reactive, ref, toRefs, onMounted, nextTick } from "vue";
import type { Ref } from "vue";
import { DialogForm } from "@/components/dialog";
import draggable from "vuedraggable";
import { SelectLink } from "@/components/select";
import { imageFormat } from "@/utils/format";
const dom: Ref<HTMLDivElement> = ref(null) as any;

const props = defineProps({
    isMultiple: {
        type: Boolean,
        default: false
    },
    showDesc: {
        type: Boolean,
        default: false
    },
    decorateType: {
        type: String,
        default: ""
    }
});
// 动态解析props
const photos = defineModel<any>("photos", { type: Array, default: [] });
// const emit = defineEmits(['update:photo'])
const { isMultiple } = toRefs(props);

const onEdit = (result: any, data: any) => {
    Object.assign(photos.value[data.index], result[0]);
};
const onEditActive = (result: any, data: any) => {
    photos.value[data.index].picActiveThumb = result[0].picThumb;
    photos.value[data.index].picActiveUrl = result[0].picUrl;
};
const onAdd = () => {
    photos.value.splice(photos.value.length - 1, 0, {
        picTitle: "",
        picThumb: "",
        picUrl: "",
        picActiveThumb: "",
        picActiveUrl: "",
        picLink: ""
    });
};
const removePic = (index: number) => {
    photos.value.splice(<any>index, 1);
};
</script>

<style lang="less" scoped>
.dec-pic-group .dec-pic-group-item .dec-pic-group-item-con .item-image {
    width: 130px;
    display: flex;
    justify-content: space-between;
}
.item-text {
    position: absolute;
    bottom: -18px;
    width: 100%;
    color: #999;
}
</style>
