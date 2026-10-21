type ShopUserPayloadSource = {
    username?: string;
    mobile?: string;
    email?: string;
    avatar?: string;
    isUsing?: number;
    roleId?: number;
};

export type ShopUserCreatePayload = Pick<
    ShopUserPayloadSource,
    "username" | "mobile" | "email" | "avatar" | "isUsing" | "roleId"
>;

export type ShopUserUpdatePayload = ShopUserCreatePayload & { id: number };

export const isValidShopUserRoleId = (roleId: number | null | undefined): boolean => Number(roleId) > 0;

export const buildShopUserCreatePayload = (formState: ShopUserPayloadSource): ShopUserCreatePayload => ({
    username: formState.username,
    mobile: formState.mobile,
    email: formState.email,
    avatar: formState.avatar,
    isUsing: formState.isUsing,
    roleId: formState.roleId
});

export const buildShopUserUpdatePayload = (id: number, formState: ShopUserPayloadSource): ShopUserUpdatePayload => ({
    id,
    ...buildShopUserCreatePayload(formState)
});
