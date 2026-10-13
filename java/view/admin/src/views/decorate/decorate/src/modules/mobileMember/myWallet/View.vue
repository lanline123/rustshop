<template>
    <div class="module-ad-con module-member-my-wallet" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <!-- Style 1: 原始钱包样式（9bdbe281b 基准：标题 + 进入钱包箭头 + 三列数据） -->
            <template v-if="layoutStyle === 1">
                <div class="wallet-style1-wrap" :style="style1WrapStyle">
                    <div class="wallet-style1-container" :style="containerStyle">
                        <div class="wallet-style1-header">
                            <div class="wallet-style1-title">{{ $t('我的钱包') }}</div>
                            <div class="wallet-style1-link">
                                <span>{{ $t('进入钱包') }}</span>
                                <span class="iconfont-h5 icon-a-gengduo20"></span>
                            </div>
                        </div>
                        <div class="wallet-style1-stats">
                            <div class="wallet-style1-stat">
                                <div class="wallet-style1-value">999.99</div>
                                <div class="wallet-style1-label">{{ $t('余额') }}</div>
                            </div>
                            <div class="wallet-style1-stat">
                                <div class="wallet-style1-value">99</div>
                                <div class="wallet-style1-label">{{ $t('优惠券') }}</div>
                            </div>
                            <div class="wallet-style1-stat">
                                <div class="wallet-style1-value">9999999</div>
                                <div class="wallet-style1-label">{{ $t('积分') }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
            <!-- Style 2: 当前钱包样式（图标 + 竖分割线 + 三列数据） -->
            <template v-else>
                <div class="my-wallet-wrap" :style="wrapStyle">
                    <div class="my-wallet-container" :style="containerStyle">
                        <div class="my-wallet-icon-section">
                            <img class="my-wallet-icon" :src="walletIconUrl" alt="钱包" />
                            <div class="my-wallet-icon-label">{{ $t('我的钱包') }}</div>
                        </div>
                        <div class="my-wallet-divider"></div>
                        <div class="my-wallet-stats">
                            <div class="my-wallet-stat">
                                <div class="my-wallet-value">640826.82</div>
                                <div class="my-wallet-label">{{ $t('余额') }}</div>
                            </div>
                            <div class="my-wallet-stat">
                                <div class="my-wallet-value">328</div>
                                <div class="my-wallet-label">{{ $t('优惠券') }}</div>
                            </div>
                            <div class="my-wallet-stat">
                                <div class="my-wallet-value">24086062</div>
                                <div class="my-wallet-label">{{ $t('积分') }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, Ref } from "vue";
import { ModuleType, ModuleIcoPicType, ModuleFrameType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";

type ModuleMyWalletType = Omit<ModuleType, "frame"> & {
    layoutStyle?: number;
    walletIcon?: ModuleIcoPicType;
    frame: ModuleFrameType;
};

const module = defineModel<ModuleMyWalletType>("module") as Ref<ModuleMyWalletType>;

const defaultWalletIcon = "https://oss.tigshop.com/official/img/pc/202605/1779873859zO64yVrNvp2y0lUsOz.png";
const defaultInnerPadding = 16;
const defaultModule = {
    layoutStyle: 1,
    walletIcon: { picUrl: defaultWalletIcon, picThumb: defaultWalletIcon },
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: defaultInnerPadding,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};
mergeDefaultModule(module.value, defaultModule);

const layoutStyle = computed(() => module.value.layoutStyle ?? 1);
const frame = computed(() => module.value.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));

const walletIconUrl = computed(() => module.value.walletIcon?.picUrl || defaultWalletIcon);

const wrapStyle = computed(() => ({
    backgroundColor: frame.value.backgroundColor || "#ffffff",
    borderRadius: `${frame.value.boxRadius ?? 0}px`
}));

const style1WrapStyle = computed(() => ({
    backgroundColor: frame.value.backgroundColor || "#ffffff",
    borderRadius: `${frame.value.boxRadius ?? 0}px`
}));

const containerStyle = computed(() => ({
    padding: `${frame.value.innerPadding ?? defaultInnerPadding}px`
}));
</script>
<style lang="less" scoped>
/* ======================== Style 1: 原始钱包样式（9bdbe281b 基准） ======================== */
.wallet-style1-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.wallet-style1-container {
    box-sizing: border-box;
}
.wallet-style1-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.wallet-style1-title {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
}
.wallet-style1-link {
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 13px;
    color: #999;
    .icon-a-gengduo20 {
        font-size: 10px;
    }
}
.wallet-style1-stats {
    display: flex;
    align-items: center;
    justify-content: space-around;
}
.wallet-style1-stat {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
}
.wallet-style1-value {
    font-size: 18px;
    font-weight: 500;
    color: #1a1a1a;
}
.wallet-style1-label {
    font-size: 12px;
    color: #1a1a1a;
}

/* ======================== Style 2: 当前钱包样式 ======================== */
.my-wallet-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.my-wallet-container {
    box-sizing: border-box;
    display: flex;
    align-items: center;
}
.my-wallet-icon-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex-shrink: 0;
}
.my-wallet-icon {
    width: 24px;
    height: 22px;
    object-fit: contain;
}
.my-wallet-icon-label {
    margin-top: 4px;
    font-size: 12px;
    line-height: 20px;
    color: #666;
    white-space: nowrap;
}
.my-wallet-divider {
    width: 10px;
    height: 54px;
    background-image: url("https://oss.tigshop.com/official/img/pc/202605/1779929210zBMvyOonKlS2ANuWkq.png");
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
    margin: 0 8px;
    flex-shrink: 0;
}
.my-wallet-stats {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
}
.my-wallet-stat {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}
.my-wallet-value {
    font-size: 16px;
    line-height: 24px;
    font-weight: 600;
    color: #333;
}
.my-wallet-label {
    font-size: 12px;
    color: #666;
    line-height: 20px;
}
</style>
