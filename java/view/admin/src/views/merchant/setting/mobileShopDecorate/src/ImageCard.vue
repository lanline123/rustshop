<template>
    <div class="image-card">
        <div class="left-card">
            <div class="item-image-con">
                <img class="item-image-src" v-if="photo.picThumb" :src="imageFormat(photo.picThumb)">
                <DialogForm type="gallery" class="" @okCallback="onEdit"
                            :params="{ isMultiple: false }">
                    <span class="change-image">{{ photo.picThumb ? $t('更换图片') : $t('添加图片') }}</span>
                </DialogForm>
            </div>
            <div @click="remake()" class="remake">{{ $t('重置') }}</div>
        </div>
        <div class="right-card">
            <div class="item-info-item">
                <span class="lable">{{ $t('标题') }}</span>
                <el-input :placeholder="$t('建议10个字以内，可不填')" v-model="photo.picTitle"></el-input>
            </div>
            <div class="item-info-item">
                <span class="lable">{{ $t('链接') }}</span>
                <SelectLink v-model="photo.picLink"></SelectLink>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">

import { SelectLink } from "@/components/select";
import { imageFormat } from "@/utils/format";
import { DialogForm } from "@/components/dialog";
const photo = defineModel<any>("photo", { type: Object, default: {} });
const remake = ()=>{
    photo.value = ''
}
const onEdit = (result: any, data: any) => {
    photo.value = result[0]

}
</script>
<style scoped lang="less">
.image-card {
    box-sizing: border-box;
    border-radius: 2px;
    background-color: #fff;
    box-shadow: 0 0 4px 0 rgba(10, 42, 97, 0.2);
    width: 100%;
    display: flex;
    padding: 4px;

    .left-card {
        margin: 6px 0 0 12px;
        position: relative;
        display: flex;
        flex-direction: column;

        .item-image-con {
            position: relative;
            width: 60px;
            height: 60px;
            border: 1px solid #e5e5e5;
            text-align: center;
            background: #f9f9f9;

            .item-image-src {
                box-sizing: border-box;
                vertical-align: bottom;
                height: 100%;
                width: 100%;
                -o-object-fit: contain;
                object-fit: contain;
            }
            .change-image{
                position: absolute;
                bottom: 0;
                left: 0;
                width: 100%;
                height: 20px;
                line-height: 20px;
                font-size: 12px;
                color: #fff;
                background: rgba(0, 0, 0, 0.5);
                cursor: pointer;
            }
        }
        .remake{
            height: 14px;
            color:var(--tig-primary);
            text-align: center;
            line-height: 20px;
            cursor: pointer;
        }
    }

    .right-card {
        display: flex;
        flex-direction: column;
        flex: 1;

        .item-info-item {
            padding: 6px 12px;
            display: flex;
            justify-content: flex-start;
            align-items: center;

            .lable {
                margin-right: 16px;
                font-size: 14px;
                color: #323233;
                line-height: 18px;
                white-space: nowrap;
            }
        }
    }
}
</style>
