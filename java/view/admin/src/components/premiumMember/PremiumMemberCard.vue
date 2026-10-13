<template>
    <div :class="['premium-member-card-wrap', `premium-member-card-wrap--${variant}`]">
        <el-popover v-if="variant === 'list'" placement="top-start" trigger="hover" :width="456" :show-after="120">
            <template #reference>
                <div class="premium-member-card__list-reference">
                    <div class="premium-member-card premium-member-card--list" :style="{ background: cardBackground }">
                        <div class="premium-member-card__image" :style="imageStyle"></div>
                        <div class="premium-member-card__content">
                            <strong>{{ cardName || $t("权益卡") }}</strong>
                            <span v-if="showCardNo">{{ cardNo || $t("卡号：13888888888") }}</span>
                            <span v-if="showValidity" class="premium-member-card__expiry">{{ expiry || $t("有效期至：2088-08-08") }}</span>
                        </div>
                    </div>
                    <span class="premium-member-card__list-name" :title="cardName || $t('权益卡')">{{ cardName || $t("权益卡") }}</span>
                </div>
            </template>
            <div class="premium-member-card premium-member-card--list-preview" :style="{ background: cardBackground }">
                <div class="premium-member-card__image" :style="imageStyle"></div>
                <div class="premium-member-card__content">
                    <strong>{{ cardName || $t("权益卡") }}</strong>
                    <span v-if="showCardNo">{{ cardNo || $t("卡号：13888888888") }}</span>
                    <span v-if="showValidity" class="premium-member-card__expiry">{{ expiry || $t("有效期至：2088-08-08") }}</span>
                </div>
            </div>
        </el-popover>
        <div v-else class="premium-member-card" :style="{ background: cardBackground }">
            <div class="premium-member-card__image" :style="imageStyle"></div>
            <div class="premium-member-card__content">
                <strong>{{ cardName || $t("权益卡") }}</strong>
                <span v-if="showCardNo">{{ cardNo || $t("卡号：13888888888") }}</span>
                <span v-if="showValidity" class="premium-member-card__expiry">{{ expiry || $t("有效期至：2088-08-08") }}</span>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
    defineProps<{
        cardName?: string;
        cardImage?: string;
        cardTheme?: string;
        cardNo?: string;
        expiry?: string;
        showCardNo?: boolean;
        showValidity?: boolean;
        variant?: "form" | "decorate" | "list";
        metaText?: string;
    }>(),
    {
        cardTheme: "#d02d2d",
        showCardNo: true,
        showValidity: true,
        variant: "form"
    }
);
const cardBackground = computed(() => `linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0) 100%), ${props.cardTheme}`);
const imageStyle = computed(() => (props.cardImage ? { backgroundImage: `url(${props.cardImage})` } : undefined));
</script>

<style scoped lang="less">
.premium-member-card-wrap {
    min-width: 0;
}
.premium-member-card {
    display: flex;
    box-sizing: border-box;
    gap: 16px;
    overflow: hidden;
    padding: 14px;
    border-radius: 8px;
    color: #fff;
}
.premium-member-card__image {
    flex: 0 0 140px;
    width: 140px;
    height: 100%;
    min-height: 0;
    border-radius: 6px;
    background: linear-gradient(135deg, #44215d, #321a4b);
    background-position: center;
    background-repeat: no-repeat;
    background-size: contain;
}
.premium-member-card__content {
    display: flex;
    flex: 1;
    flex-direction: column;
    gap: 10px;
    min-width: 0;
    padding-top: 2px;
}
.premium-member-card__content span {
    font-size: 12px;
}
.premium-member-card__content strong {
    overflow: hidden;
    font-size: 18px;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.premium-member-card__expiry {
    margin-top: auto;
}
.premium-member-card-wrap--form .premium-member-card {
    width: 347px;
    height: 117px;
}
.premium-member-card-wrap--decorate .premium-member-card {
    width: 100%;
    height: 117px;
}
.premium-member-card-wrap--list {
    width: 100%;
    padding: 5px 0;
}
.premium-member-card__list-reference {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
}
.premium-member-card--list {
    flex: 0 0 156px;
    width: 156px;
    height: 49px;
    gap: 4px;
    padding: 4px;
    box-shadow: 0 2px 8px rgb(31 35 41 / 10%);
}
.premium-member-card--list .premium-member-card__image {
    flex: none;
    width: calc(41px * 420 / 267);
    height: 41px;
}
.premium-member-card--list .premium-member-card__content {
    overflow: hidden;
    gap: 2px;
}
.premium-member-card--list .premium-member-card__content strong {
    display: block;
    flex: 0 0 auto;
    font-size: 7px;
    line-height: 9px;
}
.premium-member-card--list .premium-member-card__content span {
    flex: 0 0 auto;
    font-size: 6px;
    line-height: 8px;
}
.premium-member-card__list-name {
    min-width: 0;
    overflow: hidden;
    color: #30343b;
    font-size: 14px;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.premium-member-card--list-preview {
    width: 430px;
    height: 150px;
    padding: 16px;
    box-shadow: 0 6px 20px rgb(31 35 41 / 14%);
}
.premium-member-card--list-preview .premium-member-card__image {
    flex: none;
    width: calc(118px * 420 / 267);
    height: 118px;
}
.premium-member-card--list-preview .premium-member-card__content {
    gap: 9px;
}
</style>
