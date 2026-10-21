import re
for f,pat in (("infrastructure/src/db/repo/member/user_rank.rs","USER_RANK_SELECT"),("infrastructure/src/db/repo/member/feedback.rs","is_order_three_way")):
    t=open(f,encoding="utf-8",errors="replace").read()
    print("==",f)
    i=t.find("const %s"%pat)
    if i>=0: print(t[i:i+300])
    # find test
    j=t.find("fn is_order_three_way")
    if j>=0: print(t[j-200:j+600])
    k=t.find("fn table_names_match_entities")
    if k>=0: print(t[k-100:k+500])
