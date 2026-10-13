import { defineStore } from "pinia";
import { ref } from "vue";
import { getCurrencyList } from "@/api/common";
import type { CurrencyItem } from "@/types/common.d.ts";
import { useStorageData } from "@/hooks";

export const useCurrencyStore = defineStore("currency", () => {
    // 货币列表
    const currencyList = ref<CurrencyItem[]>([]);
    const { data, setStorageData } = useStorageData("currencyData");
    // 当前货币数据
    const currentCurrencyData = ref<CurrencyItem>((data.value as CurrencyItem) || ({} as CurrencyItem));

    const fetchCurrencyList = async () => {
        try {
            const result = await getCurrencyList();
            currencyList.value = result;

            const defaultData = result.find((item) => item.isDefault === 1)!;

            if (data.value && data.value.id) {
                const latest = result.find((item) => item.id === data.value.id);
                setDefaultData(latest || defaultData || data.value);
                return;
            }

            setDefaultData(defaultData);
        } catch (error) {
            console.error(error);
        }
    };
    // 设置默认货币
    const setDefaultData = (currentCurrency: CurrencyItem) => {
        currentCurrencyData.value = currentCurrency;
        setStorageData(currentCurrency);
    };

    // 返回响应式属性和方法
    return {
        currencyList,
        currentCurrencyData,
        fetchCurrencyList,
        setDefaultData
    };
});
