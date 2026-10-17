import io

p = "crates/services/src/member/feedback.rs"
t = io.open(p, encoding="utf-8", errors="replace").read()
old = "assert!(!scope_denied(feedback_type::PRODUCT, None, platform));"
new = ("// Java uses `!Objects.equals(getShopId(), 0)`; Objects.equals(null, 0) == false\n"
       "        // => shop_id = NULL is also denied.\n"
       "        assert!(scope_denied(feedback_type::PRODUCT, None, platform));")
assert old in t, "assertion line not found"
io.open(p, "w", encoding="utf-8", newline="\n").write(t.replace(old, new, 1))
print("patched feedback scope test")
