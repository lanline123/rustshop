import { defineStore } from "pinia";
import { getLicensed } from "@/api/setting/licensed";
import type { LicensedFormState } from "@/types/setting/licensed";
interface State {
    licensedData: LicensedFormState;
}

export const useLicensedStore = defineStore("licensed", {
    state: (): State => {
        return <State>{
            licensedData: {
                license: "",
                licensedType: "",
                licensedTypeName: "",
                deCopyright: 0,
                isEnterprise: -1,
                authorizedDomain: "",
                releaseTime: "",
                expirationTime: "",
                licensedId: [],
                shopCompany: 1,
                poweredBy: 1,
                poweredByStatus: 0,
                versionType: "",
                version: "",
                shopCompanyTxt: "",
                defaultCopyright: "Copyright © 2024 Tigshop. All Rights Reserved",
                adminLightLogo: null,
                versionInfoHidden: 0
            }
        };
    },
    actions: {
        async _getLicensed() {
            const result = await getLicensed();
            const configString = localStorage.getItem("config");
            const config = configString ? JSON.parse(configString) : {};
            this.licensedData = Object.assign(this.licensedData, result, config);
            if (result.deCopyright == 1) {
                this.licensedData.shopCompanyTxt = config.shopCompanyTxt;
            } else {
                this.licensedData.defaultCopyright = config.defaultCopyright || "Copyright © 2024 Tigshop. All Rights Reserved";
            }
            localStorage.setItem("licensedData", JSON.stringify(this.licensedData));
        }
    }
});
