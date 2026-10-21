/**
 * 多 tab 语音播报 leader 选举
 *
 * @description 基于 navigator.locks.request 做命名锁选举，保证同账号同浏览器多 tab 只有一个 tab 发声。
 *   - 锁名：`voice-broadcaster${userKey ? ':' + userKey : ''}`，按账号隔离（CR-20260615-173101 Q6）
 *   - 抢到锁的 tab 即为 leader，与 visibilityState 无关（CR-20260615-173101 Q3=A）
 *   - leader tab 关闭 / 刷新 / 导航离开时浏览器自动释放锁，其他 tab 自动接管
 *   - 不支持 Web Locks 的浏览器（兜底）：直接视为 leader，回到"每 tab 各自播"的当前行为
 *
 * @see .ai-os/baseline-log/CR-20260615-173101-tts-multi-tab-leader-and-background-play.md
 */

/** Web Locks API 最小可用子集 */
type LockManager = {
    request: (
        name: string,
        options: { mode: "exclusive" | "shared" },
        cb: () => Promise<void>
    ) => Promise<void>;
};

class VoiceBroadcastLeader {
    /**
     * 是否为 leader。
     *
     * 状态转换：
     *  - 初始 false
     *  - 抢到锁 / 不支持 Web Locks → true
     *
     * 注意：一旦置 true 不会再回退到 false。
     *   - Web Locks 模型下 leader 失活只可能是 tab 关闭/刷新，本变量随该 tab 销毁；
     *   - 不会出现"运行中失去 leader 身份"的场景，故无需 false 转移。
     */
    private _isLeader = false;

    /** 是否已启动；防止重复调用 start 导致重复 request 锁 */
    private started = false;

    /** 当前锁名，仅供日志/调试用 */
    private lockName = "";

    /**
     * 启动选举
     *
     * @param userKey 账号隔离键；可传 adminId / userId 等稳定字符串；空字符串表示不区分账号
     */
    start(userKey: string = ""): void {
        if (this.started) return;
        this.started = true;

        this.lockName = `voice-broadcaster${userKey ? ":" + userKey : ""}`;

        const locks: LockManager | undefined =
            typeof navigator !== "undefined" && (navigator as any).locks
                ? ((navigator as any).locks as LockManager)
                : undefined;

        // 兜底：环境不支持 Web Locks 时直接视为 leader，避免完全静音
        if (!locks) {
            this._isLeader = true;
            return;
        }

        // 抢锁：锁内 Promise 永不 resolve，本 tab 关闭/刷新/导航离开时浏览器自动释放
        // 其他 pending 的 request 会自动获得锁、成为新 leader
        locks
            .request(this.lockName, { mode: "exclusive" }, () => {
                this._isLeader = true;
                return new Promise<void>(() => {
                    /* never resolve；锁的生命周期 = tab 的生命周期 */
                });
            })
            .catch(() => {
                /* 抢锁链路异常（页面 unload 等）：忽略，本 tab 不再成为 leader */
            });
    }

    /** 是否当前为 leader（启动前永远 false） */
    isLeader(): boolean {
        return this._isLeader;
    }
}

/** 单例 */
export const voiceBroadcastLeader = new VoiceBroadcastLeader();
