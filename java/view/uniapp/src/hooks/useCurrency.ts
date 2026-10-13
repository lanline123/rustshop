import { useConfigStore } from "@/store/config";
import { useCurrencyStore } from "@/store/currency";
import { ref } from "vue";
import { isOverseas } from "@/utils";
import { t } from "@/utils/i18n";

export const useCurrency = () => {
    const currencyStore = useCurrencyStore();
    const configStore = useConfigStore();
    const currency = ref("");
    const currencyName = ref("");
    // 主货币（钱包/分销类金额恒用后台主货币，不随展示币种切换）
    const baseCurrency = ref("");
    const baseCurrencyName = ref("");

    const getCurrency = () => {
        currency.value = isOverseas() ? currencyStore.currentCurrencyData.symbol : configStore.dollarSign;
    };

    const getCurrencyName = () => {
        currencyName.value = isOverseas() ? currencyStore.currentCurrencyData.name : configStore.dollarSignCn;
    };

    const getBaseCurrency = () => {
        baseCurrency.value = configStore.dollarSign;
    };

    const getBaseCurrencyName = () => {
        // 主货币名走 i18n 翻译，避免海外非中文用户看到未本地化的中文币名
        baseCurrencyName.value = t(configStore.dollarSignCn);
    };

    getCurrency();
    getCurrencyName();
    getBaseCurrency();
    getBaseCurrencyName();

    return {
        currency,
        currencyName,
        baseCurrency,
        baseCurrencyName
    };
};
