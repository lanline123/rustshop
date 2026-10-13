

export interface SalesmanConfigFormState {
	/** 分销模式：1-推广员等级模式（默认） 2-微商模式 */
	distributionMode: number;
	/** 下级招募方式（微商模式专用）：1-消费自动开通 2-名片邀请开通 */
	recruitMethods: number[];
	/** 分销隐私：是否允许分销员查看自己的客户信息，0-关闭 1-开启 */
	viewCustomerInfo: number;
	/** 当前部署是否支持微商模式（后端按版本裁剪返回，前端据此显隐微商卡片） */
	wechatBizEnabled?: boolean;
	saleType: number;
	registerToSalesman: number;
	level: Level[];
}

export interface Self_buy_amount {
	checked: boolean;
	value: number;
	title: string;
	unit: string;
}

export interface Sales_amount {
	checked: boolean;
	value: number;
	title: string;
	unit: string;
}

export interface Sales_invite_user {
	checked: boolean;
	value: number;
	title: string;
	unit: string;
}

export interface Invite_sale {
	checked: boolean;
	value: number;
	title: string;
	unit: string;
}

export interface Condition {
	selfBuyAmount: Self_buy_amount;
	salesAmount: Sales_amount;
	salesInviteUsers: Sales_invite_user;
	inviteSales: Invite_sale;
}

export interface Level {
	id: number;
	name: string;
	rate: number;
	condition: Condition;
	downSalesmanRate: number;
}

