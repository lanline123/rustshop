import io

p = "crates/infrastructure/src/db/repo/member/user_rank.rs"
t = io.open(p, encoding="utf-8", errors="replace").read()
old = 'assert!(USER_RANK_CONFIG_SELECT.contains("FROM user_rank_config "));'
new = 'assert!(USER_RANK_CONFIG_SELECT.contains("FROM user_rank_config"));'
assert old in t
io.open(p, "w", encoding="utf-8", newline="\n").write(t.replace(old, new, 1))
print("patched user_rank_config assertion")
