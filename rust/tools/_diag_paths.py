import sys
import os
import json
sys.stdout.reconfigure(encoding="utf-8", errors="replace")

HERE = os.path.dirname(os.path.abspath(__file__))
d = json.load(open(os.path.join(HERE, "..", "docs", "api-contract.json"), encoding="utf-8"))

def find(mod, frag, label):
    print("=== %s :: %s ===" % (mod, label))
    for e in d["endpoints"]:
        if e["module"] == mod and frag in e["path"]:
            print("  %-6s %-50s %-24s %s" % (e["method"], e["path"], e["handler"], e["summary"][:26]))
    print()

find("adminapi", "/adminapi/system/", "系统设置")
find("adminapi", "profile", "个人")
find("adminapi", "/adminapi/product/category/", "商品分类")
find("api", "/api/user/regist", "注册")
find("api", "/api/home/home/getSeckill", "首页秒杀")
find("api", "/api/article/", "文章")
find("api", "/api/user/coupon/", "用户优惠券")
find("adminapi", "article/setShow", "文章显示")
find("adminapi", "refresh", "刷新令牌")
# 搜索 admin 里所有 handler 为 refresh 的
print("=== handler == refresh ===")
for e in d["endpoints"]:
    if e["handler"] == "refresh":
        print("  %-6s %-50s %s" % (e["method"], e["path"], e["module"]))
