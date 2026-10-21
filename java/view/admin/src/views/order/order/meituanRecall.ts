type RecallAction = (orderId: number) => Promise<unknown>;
type RefreshAction = () => Promise<unknown>;

type MeituanRecallOperationResult = { status: "success" } | { status: "cancel_failed" | "call_failed"; error: unknown };

export type MeituanRecallResult =
    | (MeituanRecallOperationResult & { refreshSucceeded: true })
    | (MeituanRecallOperationResult & { refreshSucceeded: false; refreshError: unknown });

interface ExecuteMeituanRecallOptions {
    orderId: number;
    cancel: RecallAction;
    callRider: RecallAction;
    refresh: RefreshAction;
}

/**
 * 前端只能按既有契约顺序调用 cancel → callRider，无法提供跨请求原子性。
 * 这里保留失败阶段并在所有结果下刷新列表，让调用方能够展示真实状态和可恢复路径。
 */
export const executeMeituanRecall = async ({ orderId, cancel, callRider, refresh }: ExecuteMeituanRecallOptions): Promise<MeituanRecallResult> => {
    let operationResult: MeituanRecallOperationResult;

    try {
        await cancel(orderId);
    } catch (error: unknown) {
        operationResult = { status: "cancel_failed", error };
        return appendRefreshResult(operationResult, refresh);
    }

    try {
        await callRider(orderId);
        operationResult = { status: "success" };
    } catch (error: unknown) {
        operationResult = { status: "call_failed", error };
    }

    return appendRefreshResult(operationResult, refresh);
};

const appendRefreshResult = async (
    operationResult: MeituanRecallOperationResult,
    refresh: RefreshAction
): Promise<MeituanRecallResult> => {
    try {
        await refresh();
        return { ...operationResult, refreshSucceeded: true } as MeituanRecallResult;
    } catch (refreshError: unknown) {
        return { ...operationResult, refreshSucceeded: false, refreshError } as MeituanRecallResult;
    }
};
