<template>
    <el-form label-width="120px">
        <div class="formTitle">{{ $t('页面装修') }}</div>
        <el-form-item :label="$t('背景图')">
            <div>
                <div class="form-item-row" style="align-items: flex-end">
                    <FormAddGallery v-model:photo="formState.decorateConfig.bgImage" :isLocal="true" pageType="overseas" :data-type="13"></FormAddGallery>
                    <el-button style="height: 20px" type="primary" link @click="resetBackgroundImage">{{ $t('恢复默认图') }}</el-button>
                </div>
                <div class="tip">{{ $t('建议尺寸 750x1625 px (手机全屏)') }}</div>
            </div>
        </el-form-item>
        <el-form-item :label="$t('背景色')">
            <div class="form-item-row">
                <el-color-picker v-model="formState.decorateConfig.bgColor" show-alpha :predefine="predefineColors" class="color-picker-rect" />
                <el-button type="primary" link @click="resetBackgroundColor">{{ $t('重置') }}</el-button>
            </div>
        </el-form-item>
        <el-form-item :label="$t('奖品入口图')">
            <div>
                <div class="form-item-row" style="align-items: flex-end">
                    <FormAddGallery
                        v-model:photo="formState.decorateConfig.prizeEntryImage"
                        :isLocal="true"
                        pageType="overseas"
                        :data-type="13"
                    ></FormAddGallery>
                    <el-button style="height: 20px" type="primary" link @click="resetPrizeEntryImage">{{ $t('恢复默认图') }}</el-button>
                </div>
            </div>
        </el-form-item>
        <el-form-item :label="$t('中奖名单')">
            <el-radio-group v-model="formState.decorateConfig.showWinnerList">
                <el-radio :value="1">{{ $t('显示') }}</el-radio>
                <el-radio :value="0">{{ $t('不显示') }}</el-radio>
            </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('玩法规则')">
            <div class="multilingual-textarea-overlay" style="position: relative; display: inline-block; width: 500px">
                <TigInput
                    v-model="formState.decorateConfig.activityRules"
                    type="textarea"
                    :rows="6"
                    :maxlength="500"
                    :show-word-limit="true"
                    :wordLimitPosition="'outside'"
                    :placeholder="$t('请输入活动规则说明')"
                    style="width: 100%"
                />
                <BusinessData
                    v-model:modelValue="formState.decorateConfig.activityRules"
                    type="textarea"
                    :placeholder="$t('请输入活动规则说明')"
                    :dataType="12"
                ></BusinessData>
            </div>
        </el-form-item>
    </el-form>
</template>

<script setup lang="ts">
import { ref } from "vue";
import type { LotteryFormState } from "@/types/promotion/luckyWheel.d.ts";
import { FormAddGallery } from "@/components/gallery";
import BusinessData from "@/components/multilingual/BusinessData.vue";

const formState = defineModel<LotteryFormState>({ required: true });

const predefineColors = ref([
    "#ff4500",
    "#ff8c00",
    "#ffd700",
    "#90ee90",
    "#00ced1",
    "#1e90ff",
    "#c71585",
    "#EE0A24",
    "#FF6B6B",
    "#FFD93D",
    "#6BCB77",
    "#4D96FF"
]);

// 默认背景图
const defaultBackgroundImage = "https://oss.tigshop.com/img/gallery/202512/1766469537jXrMsUPDXoszzk0KCC.jpeg";
// 默认奖品入口图片
const defaultPrizeEntryImage = "https://oss.tigshop.com/img/gallery/202601/1769405706W6G6mAb5dVkUxQIN7g.jpeg";
// 恢复默认背景图
const resetBackgroundImage = () => {
    formState.value.decorateConfig.bgImage = defaultBackgroundImage;
};
// 恢复默认奖品入口图
const resetPrizeEntryImage = () => {
    formState.value.decorateConfig.prizeEntryImage = defaultPrizeEntryImage;
};

// 重置背景色
const resetBackgroundColor = () => {
    formState.value.decorateConfig.bgColor = "transparent";
};

const validate = async () => {
    return true;
};

defineExpose({ validate });
</script>

<style scoped lang="less">
.tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
}

.form-item-row {
    display: flex;
    align-items: center;
    gap: 12px;
}

:deep(.el-color-picker) {
    width: 70px;
}

.color-picker-rect {
    :deep(.el-color-picker__trigger) {
        width: 80px;
        height: 32px;
        border-radius: 4px;

        .el-color-picker__color {
            border-radius: 4px;
        }

        .el-color-picker__icon {
            display: none;
        }
    }
}

/* 给右下角翻译按钮预留空间，避免遮挡输入文字 */
:deep(.multilingual-textarea-overlay .el-textarea__inner) {
    padding-bottom: 44px;
}
</style>
