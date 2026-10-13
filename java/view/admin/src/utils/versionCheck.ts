/**
 * 是否因版本不一致需要清会话。
 * 本地无 version 视为首次访问，只记版本、不清会话；仅「已有旧 version 且不等于当前」才清。
 */
export function needsVersionMismatchClear(storedVersion: string | null, currentVersion: string): boolean {
    if (!storedVersion) return false
    return storedVersion !== currentVersion
}
