#!/usr/bin/env python3
"""临时兼容层：把 Rust 旧版 /adminapi/panel/panel/index 的扁平响应
补齐成原版前端期望的 PanelIndexVO 嵌套结构。

只监听 127.0.0.1:8090，由 nginx 仅对 exact path 转发；不修改原前端。
"""
from http.server import BaseHTTPRequestHandler, HTTPServer
from socketserver import ThreadingMixIn
import json
import urllib.error
import urllib.request

BACKEND = "http://127.0.0.1:8080"
PANEL_PATH = "/adminapi/panel/panel/index"
LOGISTICS_PATHS = {
    "/adminapi/setting/logisticsCompany/list",
    "/adminapi/setting/logisticsCompanyConfig/list",
}


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def do_GET(self):
        path = self.path.split("?", 1)[0]
        if path != PANEL_PATH and path not in LOGISTICS_PATHS:
            self.send_response(404)
            self.send_header("Content-Length", "0")
            self.end_headers()
            return

        headers = {}
        for key in (
            "Authorization",
            "X-ADMIN-TYPE",
            "X-CLIENT-TYPE",
            "X-Locale-Code",
            "X-Shop-Id",
            "X-Vendor-Id",
        ):
            value = self.headers.get(key)
            if value:
                headers[key] = value

        req = urllib.request.Request(BACKEND + self.path, headers=headers)
        try:
            with urllib.request.urlopen(req, timeout=20) as resp:
                body = resp.read()
                status = resp.status
        except urllib.error.HTTPError as exc:
            body = exc.read()
            status = exc.code
        except Exception as exc:  # noqa: BLE001
            body = json.dumps({"code": 500, "msg": str(exc)}, ensure_ascii=False).encode()
            status = 500

        try:
            obj = json.loads(body)
            data = obj.get("data") if isinstance(obj, dict) else None
            if path in LOGISTICS_PATHS and isinstance(data, list):
                records = []
                for item in data:
                    if not isinstance(item, dict):
                        continue
                    records.append({
                        "logisticsId": item.get("logisticsId", item.get("companyId", 0)),
                        "logisticsCode": item.get("logisticsCode", ""),
                        "logisticsName": item.get("logisticsName", item.get("companyName", "")),
                        "logisticsDesc": item.get("logisticsDesc", ""),
                        "sortOrder": item.get("sortOrder", 0),
                        "isShow": item.get("isShow", 1),
                        "isEnabled": item.get("isEnabled", 1),
                        "isBound": item.get("isBound", 0),
                    })
                total = len(records)
                obj["data"] = {
                    "records": records,
                    "list": records,
                    "total": total,
                    "size": total or 20,
                    "current": 1,
                    "pages": 1 if total else 0,
                    "totalPage": 1 if total else 0,
                    "pageSize": total or 20,
                    "page": 1,
                }
                body = json.dumps(obj, ensure_ascii=False).encode("utf-8")
            elif isinstance(data, dict):
                def pick(name, default=0):
                    value = data.get(name, default)
                    return value if value is not None else default

                data["consoleData"] = {
                    **({"productOnSaleNum": pick("productOnSaleNum"),
                        "productTotalNum": pick("productTotalNum"),
                        "todayBuyerNum": pick("todayBuyerNum"),
                        "todayOrderAmount": pick("todayOrderAmount"),
                        "todayOrderNum": pick("todayOrderNum"),
                        "todayViewNum": pick("todayViewNum"),
                        "todayVisitNum": pick("todayVisitNum")}),
                    **(data.get("consoleData") or {}),
                    "awaitPay": pick("awaitPay"),
                    "awaitShip": pick("awaitShip"),
                    "awaitAfterSale": pick("awaitAfterSale"),
                    "awaitComment": pick("awaitComment"),
                }
                data["realTimeData"] = {
                    "todayOrderAmount": pick("todayOrderAmount"),
                    "yesterdayOrderAmount": pick("yesterdayOrderAmount"),
                    "orderAmountGrowthRate": pick("orderAmountGrowthRate"),
                    "todayVisitNum": pick("todayVisitNum"),
                    "yesterdayVisitNum": pick("yesterdayVisitNum"),
                    "visitGrowthRate": pick("visitGrowthRate"),
                    "todayBuyerNum": pick("todayBuyerNum"),
                    "yesterdayBuyerNum": pick("yesterdayBuyerNum"),
                    "buyerGrowthRate": pick("buyerGrowthRate"),
                    "todayViewNum": pick("todayViewNum"),
                    "yesterdayViewNum": pick("yesterdayViewNum"),
                    "viewGrowthRate": pick("viewGrowthRate"),
                    "todayOrderNum": pick("todayOrderNum"),
                    "yesterdayOrderNum": pick("yesterdayOrderNum"),
                    "orderGrowthRate": pick("orderGrowthRate"),
                }
                body = json.dumps(obj, ensure_ascii=False).encode("utf-8")
        except Exception:
            pass

        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, fmt, *args):
        pass


class ThreadingHTTPServer(ThreadingMixIn, HTTPServer):
    daemon_threads = True


if __name__ == "__main__":
    ThreadingHTTPServer(("127.0.0.1", 8090), Handler).serve_forever()
