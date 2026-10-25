"""从 docs/api-contract.json 生成管理端权限表。

输出：crates/api/admin/src/generated_permissions.rs
- 仅包含 `module=adminapi` 且 `permissions` 非空的端点；
- 路径中的 `{param}` 按「单段通配」匹配。
"""
import io
import json
import re

SRC = "docs/api-contract.json"
OUT = "crates/api/admin/src/support/generated_permissions.rs"

data = json.load(io.open(SRC, encoding="utf-8"))
rows = []
for e in data["endpoints"]:
    if e.get("module") != "adminapi":
        continue
    perms = e.get("permissions") or []
    if not perms:
        continue
    rows.append((e["method"], e["path"], sorted(set(perms))))

rows.sort(key=lambda r: (r[0], r[1]))

lines = []
lines.append("//! 管理端接口权限表（由 `tools/compat/gen_admin_permissions.py` 自动生成，勿手改）。")
lines.append("//!")
lines.append("//! 数据来源：`docs/api-contract.json` 中 `module=adminapi` 且带 `permissions` 的端点。")
lines.append("//! 语义与 Java `@PreAuthorize(\"@pms.hasPermission(...)\")` 一致：**任意一个**权限匹配即放行；")
lines.append("//! 权限串支持 Spring `PatternMatchUtils.simpleMatch` 风格（`*` 通配）。")
lines.append("")
lines.append("/// `(method, path_pattern, required_permissions[: any-of])`。")
lines.append("pub static PERMISSIONS: &[(&str, &str, &[&str])] = &[")
for method, path, perms in rows:
    perm_lit = ", ".join(json.dumps(p, ensure_ascii=False) for p in perms)
    lines.append(f'    ({json.dumps(method, ensure_ascii=False)}, {json.dumps(path, ensure_ascii=False)}, &[{perm_lit}]),')
lines.append("];")
lines.append("")
lines.append("/// 按方法 + 实际请求路径查所需权限（无条目 → None，表示不校验）。")
lines.append("#[must_use]")
lines.append("pub fn required(method: &str, path: &str) -> Option<&'static [&'static str]> {")
lines.append("    PERMISSIONS")
lines.append("        .iter()")
lines.append("        .find(|(m, p, _)| *m == method && path_matches(p, path))")
lines.append("        .map(|(_, _, perms)| *perms)")
lines.append("}")
lines.append("")
lines.append("/// `{param}` 单段通配匹配。")
lines.append("#[must_use]")
lines.append("pub fn path_matches(pattern: &str, path: &str) -> bool {")
lines.append("    if !pattern.contains('{') {")
lines.append("        return pattern == path;")
lines.append("    }")
lines.append("    let p: Vec<&str> = pattern.trim_matches('/').split('/').collect();")
lines.append("    let a: Vec<&str> = path.trim_matches('/').split('/').collect();")
lines.append("    if p.len() != a.len() {")
lines.append("        return false;")
lines.append("    }")
lines.append("    p.iter().zip(a.iter()).all(|(pat, seg)| {")
lines.append("        (pat.starts_with('{') && pat.ends_with('}')) || pat == seg")
lines.append("    })")
lines.append("}")
lines.append("")
lines.append("#[cfg(test)]")
lines.append("mod tests {")
lines.append("    use super::*;")
lines.append("")
lines.append("    #[test]")
lines.append("    fn table_generated() {")
lines.append("        assert!(!PERMISSIONS.is_empty());")
lines.append("        assert!(PERMISSIONS.iter().all(|(_, _, perms)| !perms.is_empty()));")
lines.append("    }")
lines.append("")
lines.append("    #[test]")
lines.append("    fn looks_up_known_endpoint() {")
lines.append("        let perms = required(\"GET\", \"/adminapi/authority/adminRole/list\");")
lines.append("        assert!(perms.is_some());")
lines.append("        assert!(perms.unwrap().contains(&\"adminRoleManage\"));")
lines.append("        assert!(required(\"GET\", \"/adminapi/not/exists\").is_none());")
lines.append("    }")
lines.append("")
lines.append("    #[test]")
lines.append("    fn path_param_matching() {")
lines.append("        assert!(path_matches(\"/a/{id}/b\", \"/a/123/b\"));")
lines.append("        assert!(!path_matches(\"/a/{id}/b\", \"/a/123/c\"));")
lines.append("        assert!(!path_matches(\"/a/{id}/b\", \"/a/123/b/c\"));")
lines.append("    }")
lines.append("}")

io.open(OUT, "w", encoding="utf-8", newline="\n").write("\n".join(lines) + "\n")
print(f"generated {OUT}: {len(rows)} permission endpoints")
