<template>
    <div v-if="module.image.picUrl" class="image-bar">
        <el-image :class="['box-size']" :src="imageFormat(module.image.picUrl)"></el-image>
    </div>
    <div class="controls-bar" :style="{ backgroundColor: module.backgroundColor }">
        <div class="info-bar">
            <div class="left-bar">
                <div :style="{ color: module.textColor }" class="bar-item">{{ $t('首页') }}</div>
                <div :style="{ color: module.textColor }" class="bar-item">{{ $t('全部分类') }}</div>
                <div :style="{ color: module.textColor }" class="bar-item">{{ $t('服装') }}</div>
                <div :style="{ color: module.textColor }" class="bar-item">{{ $t('食品') }}</div>
            </div>
            <div class="right-bar">
                <el-input size="small" v-model="input" style="max-width: 240px" :placeholder="$t('请输入关键字')">
                    <template #append>
                        <el-button class="query">{{ $t('搜索') }}</el-button>
                    </template>
                </el-input>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { computed, ref } from "vue";
import { imageFormat } from "@/utils/format";

const module = defineModel<any>("module", { type: Object, default: {} });

const input = ref("");
const menuList = ref([]);
const hoverColor = computed(() => {
    const color = module.value.hoverColor || "#333";
    // 将 hex 颜色转换为 rgba，透明度为 0.2
    let hex = color.replace("#", "");
    if (hex.length === 3) {
        hex = hex
            .split("")
            .map((c: string) => c + c)
            .join("");
    }
    const r = parseInt(hex.substring(0, 2), 16);
    const g = parseInt(hex.substring(2, 4), 16);
    const b = parseInt(hex.substring(4, 6), 16);
    return `rgba(${r}, ${g}, ${b}, 0.2)`;
});
</script>
<style scoped lang="less">
.controls-bar {
    display: flex;
    width: 100%;
    background-color: #158fce;
    height: 50px;
    align-items: center;
    justify-content: center;

    .info-bar {
        width: 85%;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .left-bar {
            display: flex;
            gap: 18px;

            .bar-item {
                cursor: pointer;
                height: 46px;
                color: #ffffff;
                font-weight: bold;
                line-height: 40px;
                font-size: 14px;
                display: flex;
                align-items: center;
                gap: 4px;
                padding: 2px 20px;
                // border-radius: 26px;
            }
            .bar-item:hover {
                background-color: v-bind("hoverColor") !important;
            }
        }

        .right-bar {
            display: flex;
            height: 36px;
            align-items: center;
            .query {
                background-color: #646464;
                color: white;
            }
        }
    }
}
</style>
