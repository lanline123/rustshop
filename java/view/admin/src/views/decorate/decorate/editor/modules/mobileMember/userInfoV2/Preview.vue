<template>
    <div class="module-ad-con module-member-user-info-v2" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <div class="user-info-v2-wrap" :style="wrapStyle">
                <div class="user-info-v2-container" :style="containerStyle">
                    <div class="user-info-v2-profile">
                        <div class="user-info-v2-avatar">
                            <img
                                src="https://oss.tigshop.com/img/gallery/202605/1779691335wtQOROIkUPPNOzuply.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200"
                                alt="头像"
                            />
                        </div>
                        <div class="user-info-v2-info">
                            <div class="user-info-v2-name">演示账号</div>
                            <div class="user-info-v2-level">
                                <img
                                    class="user-info-v2-level-icon"
                                    src="https://oss.tigshop.com/img/gallery/202605/17796914549H8jHXMHCnnkf8uOXg.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200"
                                    alt=""
                                />
                                <span class="user-info-v2-level-text">V6会员</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { userInfoV2Defaults } from "./index";
import type { UserInfoV2Module } from "./types";

const props = defineProps<{ module: UserInfoV2Module }>();
const module = props.module;

const defaultInnerPadding = 16;
mergeDefaultModule(module, userInfoV2Defaults);

const frame = computed(() => module.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));

const wrapStyle = computed(() => {
    return {
        backgroundColor: frame.value.backgroundColor || "#ffffff",
        borderRadius: `${frame.value.boxRadius ?? 0}px`
    };
});

const containerStyle = computed(() => {
    return {
        padding: `${frame.value.innerPadding ?? defaultInnerPadding}px`
    };
});
</script>
<style lang="less" scoped>
.user-info-v2-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.user-info-v2-container {
    box-sizing: border-box;
}
.user-info-v2-profile {
    display: flex;
    align-items: center;
}
.user-info-v2-avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    overflow: hidden;
    margin-right: 12px;
    flex-shrink: 0;
    background: linear-gradient(135deg, #7dd3fc, #38bdf8);
    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}
.user-info-v2-info {
    display: flex;
    flex-direction: column;
    justify-content: center;
}
.user-info-v2-name {
    font-size: 16px;
    color: #1a1a1a;
    margin-bottom: 10px;
    font-weight: 500;
}
.user-info-v2-level {
    display: flex;
    align-items: center;
    gap: 5px;
    border-radius: 10px;
}
.user-info-v2-level-icon {
    width: 16px;
}
.user-info-v2-level-text {
    font-size: 11px;
    color: #1a1a1a;
}
</style>
