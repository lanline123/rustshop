
export interface FriendLinksFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
}

export interface FriendLinksFilterResult {
    records: FriendLinksFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface FriendLinksFilterState {
    linkId: number;
    linkTitle?: string;
    linkLogo?: string;
    linkUrl?: string;
    sortOrder?: number;
}

export interface FriendLinksFormResult {
    item: FriendLinksFormState;
}

export interface FriendLinksFormState {
    linkId?: number;
    linkTitle?: string;
    linkLogo?: string;
    linkUrl?: string;
    sortOrder?: number;
}
