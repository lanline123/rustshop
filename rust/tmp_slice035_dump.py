import json, io

p = r"D:\tigshop\java-b2b2b-pro-v5.8.57\rust\docs\api-contract.json"
data = json.load(io.open(p, encoding="utf-8"))
paths = [
    "/adminapi/promotion/coupon/create",
    "/adminapi/promotion/coupon/update",
    "/adminapi/promotion/coupon/del",
    "/adminapi/promotion/coupon/updateField",
    "/adminapi/promotion/coupon/batch",
    "/adminapi/promotion/coupon/config",
    "/adminapi/promotion/coupon/issue/preview",
    "/adminapi/promotion/coupon/issue/log",
    "/adminapi/promotion/coupon/issue/batch",
]
eps = data["endpoints"]
for ep in eps:
    if ep["path"] in paths and ep["module"] == "adminapi":
        print(json.dumps(ep, ensure_ascii=False, indent=2))
