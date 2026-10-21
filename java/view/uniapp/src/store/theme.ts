import { defineStore } from "pinia";
import { ref } from "vue";
import { themeSettings } from "@/api/common";

export const useThemeStore = defineStore("theme", () => {
    const themeId = ref(0);

    const themeStyle = ref<{ [key: string]: string }>({});

    async function getThemeSettings() {
        if (uni.getStorageSync("themeStyle")) {
            themeStyle.value = uni.getStorageSync("themeStyle");
        }
        try {
            const res = await themeSettings();

            const data = JSON.parse(res.themeStyle);

            if (Object.keys(themeStyle.value).length === 0) {
                themeStyle.value = data;
            } else if (themeStyle.value["--general"] !== data["--general"]) {
                themeStyle.value = data;
            }

            themeStyle.value["--general"];

            uni.setStorageSync("themeStyle", data);
        } catch (error: any) {
            themeStyle.value = {
                "--general": "#ff4444",
                "--main-bg": "#ff4444",
                "--main-bg-gradient": "#ff4444",
                "--main-text": "#ffffff",
                "--vice-bg": "#ff8855",
                "--vice-text": "#fff",
                "--icon": "#FC0000",
                "--price": "#FC0000",
                "--tag-text": "#CF0000",
                "--tag-bg": "#FFF2F2",
                "--primary-light-3": "#ff7c7c",
                "--primary-light-5": "#ffa2a2",
                "--primary-light-7": "#ffc7c7",
                "--primary-light-8": "#ffdada",
                "--primary-light-9": "#ffecec",
                "--primary-dark-2": "#cc3636",
                "--ump-main-bg": "#ff4444",
                "--ump-main-text": "#ffffff",
                "--ump-vice-bg": "#ff8855",
                "--ump-vice-text": "#fff",
                "--ump-icon": "#FC0000",
                "--ump-price": "#FC0000",
                "--ump-tag-text": "#CF0000",
                "--ump-tag-bg": "#FFF2F2",
                "--ump-coupon-bg": "#FFF2F2",
                "--ump-border": "#FFCCCC",
                "--ump-start-bg": "#F86F30",
                "--ump-end-bg": "#E91717"
            };
            uni.showToast({
                title: error.message,
                icon: "none"
            });
        }
    }

    function hexToRgba(hex: string, alpha: number = 1): string {
        hex = hex.replace("#", "");
        const r = parseInt(hex.substring(0, 2), 16);
        const g = parseInt(hex.substring(2, 4), 16);
        const b = parseInt(hex.substring(4, 6), 16);

        return `rgba(${r}, ${g}, ${b}, ${alpha})`;
    }

    return {
        themeId,
        themeStyle,
        hexToRgba,
        getThemeSettings
    };
});
