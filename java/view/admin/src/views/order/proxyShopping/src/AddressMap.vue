<template>
    <div>
        <el-form-item :label="$t('所在地区')" prop="regionIds" required :rules="[{ required: true, message: '请选择所在地区!' }]">
            <SelectRegion v-model:selectedIds="regionIds" @change="onRegionChange" @clear="onClear" class="region-select-full-width" />
        </el-form-item>

        <el-form-item
            :label="$t('详细地址')"
            prop="address"
            :rules="[
                { required: true, message: '请输入详细地址!' },
                { min: 3, message: '收货地址过短' }
            ]"
        >
            <el-input v-model="detailAddress" :maxlength="100" :placeholder="$t('请输入详细地址')" />
        </el-form-item>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import SelectRegion from "./SelectRegion.vue";
import { getLocationByRegionId } from "@/api/setting/region";

const regionIds = defineModel<number[]>("regionIds");
const address = defineModel<string>("address", { default: "" });
const positionAddress = defineModel<string>("positionAddress", { default: "" });
const longitude = defineModel<number>("longitude", { default: 0 });
const latitude = defineModel<number>("latitude", { default: 0 });

const regionNameArr = ref<string[]>([]);
const detailAddress = ref<string>("");

const onRegionChange = async (regionObjects: any[]) => {
    regionNameArr.value = regionObjects?.map((x: any) => x.regionName) || [];

    const lastRegion = regionObjects?.[regionObjects.length - 1];
    const regionId = Number(lastRegion?.regionId || 0);
    if (!regionId) return;

    try {
        const res: any = await getLocationByRegionId(regionId);
        const [lng, lat] = String(res?.centor || "").split(",").map(Number);
        longitude.value = Number.isFinite(lng) ? lng : 0;
        latitude.value = Number.isFinite(lat) ? lat : 0;
    } catch {
        longitude.value = 0;
        latitude.value = 0;
    }
};

const onClear = () => {
    regionNameArr.value = [];
    address.value = "";
    detailAddress.value = "";
    positionAddress.value = "";
    longitude.value = 0;
    latitude.value = 0;
};

watch(
    () => detailAddress.value,
    (newVal) => {
        address.value = newVal ? String(newVal).trim() : "";
    },
    { immediate: true }
);

watch(
    [() => regionNameArr.value, () => detailAddress.value],
    () => {
        // 这一版不展示地图定位，但为了兼容旧字段/后端校验，自动生成 positionAddress
        // 优先写入“地区 + 详细地址”，没有地区时只用详细地址
        const regionText = (regionNameArr.value || []).filter(Boolean).join("");
        const detail = String(detailAddress.value || "").trim();
        const merged = `${regionText}${detail}`.trim();
        positionAddress.value = merged;
    },
    { immediate: true, deep: true }
);

watch(
    () => address.value,
    (newVal) => {
        const v = String(newVal || "").trim();
        if (v !== String(detailAddress.value || "").trim()) {
            detailAddress.value = v;
        }
    },
    { immediate: true }
);

onMounted(() => {
    if (address.value) detailAddress.value = String(address.value).trim();
});
</script>

<style lang="less" scoped>
.region-select-full-width {
    width: 260px;
}
</style>
