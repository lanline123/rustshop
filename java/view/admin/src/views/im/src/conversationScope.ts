import type { MessageInfo } from "@/types/im/im.d";

export type ConversationTarget = Readonly<MessageInfo>;

const hasValue = (value: unknown) => value !== undefined && value !== null;

export const isCompleteConversationTarget = (target?: ConversationTarget): target is Required<MessageInfo> => {
    return !!target && hasValue(target.conversationId) && hasValue(target.userId) && hasValue(target.shopId);
};

export const isSameConversation = (targetConversationId: unknown, currentConversationId: unknown): boolean => {
    return hasValue(targetConversationId) && hasValue(currentConversationId) && String(targetConversationId) === String(currentConversationId);
};

export const createMediaTargetRegistry = () => {
    const targets = new Map<string, ConversationTarget>();

    return {
        capture(key: string, source: MessageInfo): ConversationTarget {
            const target = Object.freeze({ ...source });
            targets.set(key, target);
            return target;
        },
        get(key: string): ConversationTarget | undefined {
            return targets.get(key);
        },
        remove(key: string): void {
            targets.delete(key);
        }
    };
};
