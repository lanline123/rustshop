// 获取详情返回参数类型

export interface MailFormResult {
    license: string;
	domain: string;
}
export interface LicensedFormState {
	license: string;
	shopCompany: number;
	licensedType: string;
	licensedTypeName: string;
	deCopyright: number;
	isEnterprise: number;
	authorizedDomain: string;
	releaseTime: string;
	expirationTime: string;
	adminLightLogo: string | null;
	poweredBy: number;
	licensedId: number[];
	poweredByStatus: number;
	versionInfoHidden: number;
	versionType: string;
	version: string;
	shopCompanyTxt: string;
    defaultCopyright: string;
}
export interface BaseLicensedData {
	shopCompany: string;
	adminLightLogo: string | null;
	adminDarkLogo: string;
	poweredBy: string;
	poweredByStatus: number;
	versionInfoHidden: number;
}