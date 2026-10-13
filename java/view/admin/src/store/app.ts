import { defineStore } from "pinia";
import { ref } from "vue";

export const useAppStore = defineStore("app", () => {
    const headerTitle = ref("");
    const routerKey = ref(0);

    function getHeaderTitle() {
        return headerTitle.value
    }

    function setHeaderTitle(str: string) {
        headerTitle.value = str;
    }

    return {
        headerTitle,
        routerKey,
        getHeaderTitle,
        setHeaderTitle
    }
});
