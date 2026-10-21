import { ref } from "vue";

export default function useStorageData(key: string) {
    const data = ref(uni.getStorageSync(key) || null);

    function setStorageData(value: any) {
        data.value = value;
        uni.setStorageSync(key, value);
    }

    return {
        data,
        setStorageData
    };
}
