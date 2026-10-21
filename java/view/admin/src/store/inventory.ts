import { defineStore } from "pinia";
import { computed } from "vue";
import { useUserStore } from "@/store/user";
const userInfo: any = computed(() => useUserStore().userInfo);
const shopId = computed(() => {
    return Number(localStorage.getItem("shopId")) || userInfo.value.shopId;
});

export const useInventoryStore = defineStore("inventory", {
    state: () => {
        return {
            outboundOrganizationId: shopId,
            warehousingOrganizationId: 0
        };
    },
    actions: {
        setOutboundOrganizationId(id: number) {
            this.outboundOrganizationId = id;
        },
        setWarehousingOrganizationId(id: number) {
            this.warehousingOrganizationId = id;
        }
    }
});
