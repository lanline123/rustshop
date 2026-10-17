import io

edits = [
    ("crates/infrastructure/src/db/repo/member/user_rank.rs",
     'assert!(USER_RANK_SELECT.contains("FROM user_rank "));',
     'assert!(USER_RANK_SELECT.contains("FROM user_rank"));'),
    ("crates/infrastructure/src/db/repo/member/feedback.rs",
     'assert!(!sql.contains("order_id"), "{sql}");',
     'assert!(!sql.contains(" AND order_id"), "{sql}");'),
]
for path, old, new in edits:
    t = io.open(path, encoding="utf-8", errors="replace").read()
    assert old in t, path
    io.open(path, "w", encoding="utf-8", newline="\n").write(t.replace(old, new, 1))
    print("patched", path)
