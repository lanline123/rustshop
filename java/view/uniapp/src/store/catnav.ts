import { defineStore } from "pinia";
import { ref } from "vue";

export const usecatnavStore = defineStore("catnav", () => {
    const navLeft = ref(0);
    const showCatNav = ref(0);
    const currentCatnavId = ref(0);
    const catColor = ref("");

    function setNavLeft(val: number) {
        navLeft.value = val;
    }

    function setShowCatNav(val: number) {
        showCatNav.value = val;
    }

    function setCurrentCatnavId(val: number) {
        currentCatnavId.value = val;
    }

    function setCatColor(val: string) {
        catColor.value = val;
    }

    function reset() {
        navLeft.value = 0;
        showCatNav.value = 0;
        currentCatnavId.value = 0;
        catColor.value = "";
    }

    return {
        navLeft,
        showCatNav,
        currentCatnavId,
        catColor,
        setNavLeft,
        setShowCatNav,
        setCurrentCatnavId,
        setCatColor,
        reset
    };
});
