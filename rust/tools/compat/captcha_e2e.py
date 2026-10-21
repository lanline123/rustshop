import base64
import json
import re
import sys
import urllib.request

from Crypto.Cipher import AES
from Crypto.Util.Padding import pad
from PIL import Image
import io

ADMIN = "http://127.0.0.1:8080"


def aes_ecb_encrypt(key: str, plain: str) -> str:
    cipher = AES.new(key.encode(), AES.MODE_ECB)
    return base64.b64encode(cipher.encrypt(pad(plain.encode(), 16))).decode()


def post(url, data):
    req = urllib.request.Request(
        url,
        data=json.dumps(data).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req) as resp:
            return json.loads(resp.read().decode())
    except urllib.error.HTTPError as e:  # noqa: F821
        return json.loads(e.read().decode())


def get(url):
    with urllib.request.urlopen(url) as resp:
        return json.loads(resp.read().decode())


def locate_piece_x(orig_b64: str) -> int:
    img = Image.open(io.BytesIO(base64.b64decode(orig_b64))).convert("RGBA")
    w, h = img.size
    # 边框像素颜色 (255,255,255,200)
    for y in range(h):
        for x in range(w):
            r, g, b, a = img.getpixel((x, y))
            if r >= 250 and g >= 250 and b >= 250 and a >= 180:
                return x
    raise SystemExit("未找到拼图缺口")


def main():
    cap = get(ADMIN + "/adminapi/common/verification/captcha")["data"]
    token = cap["token"]
    secret = cap["secretKey"]
    x = locate_piece_x(cap["originalImageBase64"])
    print("token", token, "secret", secret, "piece_x", x)

    point = json.dumps({"x": x, "y": 5.0}, separators=(",", ":"))
    enc_point = aes_ecb_encrypt(secret, point)
    check = post(
        ADMIN + "/adminapi/common/verification/check",
        {"captchaType": "blockPuzzle", "token": token, "pointJson": enc_point},
    )
    print("check:", json.dumps(check, ensure_ascii=False))
    assert check["code"] == 0, check

    verification = aes_ecb_encrypt(secret, token + "---" + point)

    # 连续 5 次错误密码，触发验证码
    for i in range(5):
        r = post(ADMIN + "/adminapi/login/signin", {
            "username": "admin", "password": "wrong-pass-123",
            "loginType": "password", "captchaUid": "", "verifyToken": None,
        })
        print("wrong", i + 1, "->", r["code"])

    r = post(ADMIN + "/adminapi/login/signin", {
        "username": "admin", "password": "admin123",
        "loginType": "password", "captchaUid": "", "verifyToken": None,
    })
    print("login without captcha ->", r["code"], r.get("msg"))
    assert r["code"] == 1002, r

    r = post(ADMIN + "/adminapi/login/signin", {
        "username": "admin", "password": "admin123",
        "loginType": "password", "captchaUid": "", "verifyToken": verification,
    })
    print("login with captcha ->", r["code"], r.get("msg"))
    assert r["code"] == 0, r
    print("CAPTCHA E2E OK")


if __name__ == "__main__":
    main()
