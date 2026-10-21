#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Java Spring MVC controller -> authoritative API inventory.

Scans the TigShop Java backend for every ``@RestController`` and extracts the
complete HTTP surface: method, full path, path/query/body parameters, Swagger
``@Operation`` documentation, ``@PreAuthorize`` permission expressions and
whether the handler bypasses the standard response wrapper.

The Java sources are GBK encoded, so decoding tries GBK first.

Outputs (default ``rust/docs``):
  * ``api-inventory.json``  - full structured inventory
  * ``api-inventory.csv``   - flat one-row-per-endpoint sheet
  * ``API-INVENTORY.md``    - human readable, grouped by module/tag
  * ``openapi.json``        - OpenAPI 3.1 skeleton derived from the inventory

Usage:
    python tools/java_api_parser.py --summary
    python tools/java_api_parser.py --all
    python tools/java_api_parser.py --check        # CI: non-zero on parse gaps
"""

from __future__ import annotations

import argparse
import csv
import json
import os
import re
import sys
from collections import OrderedDict, defaultdict

# --------------------------------------------------------------------------
# Encoding
# --------------------------------------------------------------------------

ENCODINGS = ("gbk", "gb18030", "utf-8-sig", "utf-8", "latin-1")


def read_text(path):
    """Read a Java source file, tolerating GBK and UTF-8."""
    with open(path, "rb") as fh:
        raw = fh.read()
    for enc in ENCODINGS:
        try:
            return raw.decode(enc)
        except (UnicodeDecodeError, LookupError):
            continue
    return raw.decode("latin-1", errors="replace")


# --------------------------------------------------------------------------
# Annotation scanning helpers
# --------------------------------------------------------------------------

IDENT_RE = re.compile(r"[A-Za-z_$][\w$.]*")


def extract_annotation(text, start):
    """Parse ``@Name(args)`` beginning at ``text[start] == '@'``.

    Returns ``(name, args, end)`` where ``args`` is the raw text inside the
    parentheses (``None`` when the annotation takes no arguments).  The scanner
    is string-literal aware and bracket balanced, so nested parentheses inside
    ``@PreAuthorize("@pms.hasPermission('a','b')")`` are handled correctly.
    """
    if start >= len(text) or text[start] != "@":
        return None
    m = IDENT_RE.match(text, start + 1)
    if not m:
        return None
    name = m.group(0)
    pos = m.end()
    while pos < len(text) and text[pos] in " \t\r\n":
        pos += 1
    if pos >= len(text) or text[pos] != "(":
        return name, None, pos

    depth = 0
    in_str = None
    i = pos
    while i < len(text):
        ch = text[i]
        if in_str:
            if ch == "\\":
                i += 2
                continue
            if ch == in_str:
                in_str = None
        elif ch in "\"'":
            in_str = ch
        elif ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
            if depth == 0:
                return name, text[pos + 1 : i], i + 1
        i += 1
    return name, text[pos + 1 :], len(text)


def iter_annotations(text):
    """Yield ``(name, args, start, end)`` for every annotation in ``text``."""
    i = 0
    while True:
        i = text.find("@", i)
        if i < 0:
            return
        # Skip javadoc-style or email-ish '@' inside strings/comments crudely:
        # an annotation must be followed by an identifier.
        res = extract_annotation(text, i)
        if res is None:
            i += 1
            continue
        name, args, end = res
        yield name, args, i, end
        i = end if end > i else i + 1


def strip_comments(text):
    """Remove ``//`` and ``/* */`` comments, preserving string literals."""
    out = []
    i = 0
    n = len(text)
    while i < n:
        ch = text[i]
        if ch in "\"'":
            quote = ch
            out.append(ch)
            i += 1
            while i < n:
                if text[i] == "\\":
                    out.append(text[i : i + 2])
                    i += 2
                    continue
                out.append(text[i])
                if text[i] == quote:
                    i += 1
                    break
                i += 1
            continue
        if ch == "/" and i + 1 < n:
            nxt = text[i + 1]
            if nxt == "/":
                j = text.find("\n", i)
                i = n if j < 0 else j
                continue
            if nxt == "*":
                j = text.find("*/", i + 2)
                i = n if j < 0 else j + 2
                continue
        out.append(ch)
        i += 1
    return "".join(out)


# --------------------------------------------------------------------------
# Annotation argument parsing
# --------------------------------------------------------------------------

def split_top_level(text, sep=","):
    """Split on ``sep`` ignoring nesting and string literals."""
    parts = []
    depth = 0
    in_str = None
    cur = []
    i = 0
    while i < len(text):
        ch = text[i]
        if in_str:
            cur.append(ch)
            if ch == "\\":
                if i + 1 < len(text):
                    cur.append(text[i + 1])
                    i += 2
                    continue
            elif ch == in_str:
                in_str = None
            i += 1
            continue
        if ch in "\"'":
            in_str = ch
            cur.append(ch)
        elif ch in "([{":
            depth += 1
            cur.append(ch)
        elif ch in ")]}":
            depth -= 1
            cur.append(ch)
        elif ch == sep and depth == 0:
            parts.append("".join(cur))
            cur = []
        else:
            cur.append(ch)
        i += 1
    if cur:
        parts.append("".join(cur))
    return [p.strip() for p in parts if p.strip()]


def parse_args(args):
    """Parse annotation arguments into an ordered dict.

    Positional values land under ``"value"``; ``key = value`` pairs keep their
    key.  Values stay raw strings (quotes stripped for simple literals).
    """
    result = OrderedDict()
    if not args:
        return result
    positional = []
    for part in split_top_level(args):
        m = re.match(r"^([A-Za-z_$][\w$]*)\s*=(?!=)\s*(.*)$", part, re.DOTALL)
        if m:
            result[m.group(1)] = m.group(2).strip()
        else:
            positional.append(part)
    if positional:
        if len(positional) == 1:
            result["value"] = positional[0]
        else:
            result["value"] = positional
    return result


def strip_outer_parens(value):
    """去掉多余的包裹括号：``("/a")`` -> ``"/a"``。

    部分控制器写成 ``@RequestMapping(("/api/user/account"))``，
    多一层括号在 Java 里是合法表达式，但会让字符串字面量解析失败。
    """
    s = value.strip()
    while s.startswith("(") and s.endswith(")"):
        depth = 0
        in_str = None
        balanced = True
        for i, ch in enumerate(s):
            if in_str:
                if ch == "\\":
                    continue
                if ch == in_str:
                    in_str = None
                continue
            if ch in "\"'":
                in_str = ch
            elif ch == "(":
                depth += 1
            elif ch == ")":
                depth -= 1
                if depth == 0 and i != len(s) - 1:
                    balanced = False
                    break
        if not balanced or depth != 0:
            break
        s = s[1:-1].strip()
    return s


def unquote(value):
    """Turn a Java string literal (or array literal) into Python data."""
    if value is None:
        return None
    value = strip_outer_parens(value)
    if value.startswith("{") and value.endswith("}"):
        return [unquote(p) for p in split_top_level(value[1:-1])]
    m = re.match(r'^"((?:[^"\\]|\\.)*)"$', value, re.DOTALL)
    if m:
        body = m.group(1)
        return (
            body.replace('\\"', '"')
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\\", "\\")
        )
    return value


def annotation_str(args_map, key="value"):
    if args_map is None:
        return None
    val = args_map.get(key)
    if isinstance(val, list):
        return val[0] if val else None
    return val


# --------------------------------------------------------------------------
# Java type / parameter analysis
# --------------------------------------------------------------------------

SIMPLE_TYPES = {
    "String",
    "CharSequence",
    "Integer",
    "int",
    "Long",
    "long",
    "Short",
    "short",
    "Byte",
    "byte",
    "Double",
    "double",
    "Float",
    "float",
    "Boolean",
    "boolean",
    "BigDecimal",
    "BigInteger",
    "Date",
    "LocalDate",
    "LocalDateTime",
    "LocalTime",
    "MultipartFile",
    "Object",
    "Map",
    "List",
    "Set",
    "Collection",
}

# Types that Spring resolves from the request body rather than query/form.
BODY_HINTS = ("Param", "DTO", "Dto", "Request", "Form", "Command")

# Types treated as JSON request bodies when they are the only argument.
JSON_BODY_SUFFIXES = ("Param", "DTO", "Dto")

WRAPPER_METHOD_ANN = "IgnoreResponseAdvice"


def normalize_type(java_type):
    t = java_type.strip()
    t = re.sub(r"^(final|volatile)\s+", "", t)
    t = t.strip()
    return t


def base_type(java_type):
    """Strip generics/arrays to the bare class name."""
    t = normalize_type(java_type)
    t = t.split("<")[0]
    t = t.replace("[]", "").strip()
    return t.split(".")[-1]


def parse_param(raw):
    """Parse one Java method parameter into a structured dict."""
    raw = raw.strip()
    if not raw:
        return None
    anns = OrderedDict()
    rest = raw
    # Peel leading annotations.
    while True:
        rest_stripped = rest.lstrip()
        if not rest_stripped.startswith("@"):
            rest = rest_stripped
            break
        offset = len(rest) - len(rest_stripped)
        res = extract_annotation(rest, offset)
        if res is None:
            rest = rest_stripped
            break
        name, args, end = res
        anns[name] = parse_args(args)
        rest = rest[end:]
    rest = rest.strip()
    rest = re.sub(r"\bfinal\s+", "", rest)

    # Split trailing identifier as the parameter name.
    m = re.match(r"^(?P<type>.+?)\s+(?P<name>[A-Za-z_$]\w*)$", rest, re.DOTALL)
    if m:
        ptype = m.group("type").strip()
        pname = m.group("name")
    else:
        ptype = rest.strip()
        pname = ""

    ptype = re.sub(r"\s+", " ", ptype).strip()

    info = OrderedDict()
    info["name"] = pname
    info["java_type"] = ptype
    info["base_type"] = base_type(ptype)
    info["annotations"] = OrderedDict((k, v or OrderedDict()) for k, v in anns.items())

    # Location resolution, mirroring Spring MVC's own rules.
    location = None
    required = True
    default_value = None
    if "RequestBody" in anns:
        location = "body"
        required = annotation_str(anns["RequestBody"], "required") != "false"
    elif "PathVariable" in anns:
        location = "path"
        required = annotation_str(anns["PathVariable"], "required") != "false"
    elif "RequestParam" in anns:
        location = "query"
        required = annotation_str(anns["RequestParam"], "required") != "false"
        default_value = unquote(annotation_str(anns["RequestParam"], "defaultValue"))
    elif "RequestHeader" in anns:
        location = "header"
        required = annotation_str(anns["RequestHeader"], "required") != "false"
    elif "RequestPart" in anns:
        location = "body"
    elif "CookieValue" in anns:
        location = "cookie"
    elif "ModelAttribute" in anns:
        location = "query"
    elif base_type(ptype) in SIMPLE_TYPES or base_type(ptype).endswith("[]"):
        location = "query"
    else:
        # Unannotated POJO: Spring binds query/form fields into it.
        location = "query_object"

    info["in"] = location
    info["required"] = required
    info["default"] = default_value

    # Explicit request parameter name.
    if "RequestParam" in anns:
        info["wire_name"] = unquote(annotation_str(anns["RequestParam"], "value")) or unquote(
            annotation_str(anns["RequestParam"], "name")
        ) or pname
    elif "PathVariable" in anns:
        info["wire_name"] = unquote(annotation_str(anns["PathVariable"], "value")) or unquote(
            annotation_str(anns["PathVariable"], "name")
        ) or pname
    else:
        info["wire_name"] = pname

    if "Valid" in anns or "Validated" in anns:
        info["validated"] = True
    if base_type(ptype) == "MultipartFile":
        info["in"] = "file"
    return info


# --------------------------------------------------------------------------
# Controller parsing
# --------------------------------------------------------------------------

MAPPING_ANN = {
    "GetMapping": "GET",
    "PostMapping": "POST",
    "PutMapping": "PUT",
    "DeleteMapping": "DELETE",
    "PatchMapping": "PATCH",
}

CLASS_DECL_RE = re.compile(r"\b(?:class|interface|enum)\s+([A-Za-z_$]\w*)")

# A method signature at the start of a line: modifiers, return type, name, "(".
METHOD_RE = re.compile(
    r"^[ \t]*(?P<mods>(?:public|protected|private)\s+"
    r"(?:(?:static|final|synchronized|abstract|native|default)\s+)*)"
    r"(?P<ret>[A-Za-z_$][\w$.]*(?:\s*<[^(){};]*>)?(?:\s*\[\s*\])*)"
    r"[ \t]+(?P<name>[A-Za-z_$]\w*)[ \t]*\(",
    re.MULTILINE,
)


def balance(text, open_idx, open_ch="(", close_ch=")"):
    """Return index just past the matching close bracket."""
    depth = 0
    in_str = None
    i = open_idx
    while i < len(text):
        ch = text[i]
        if in_str:
            if ch == "\\":
                i += 2
                continue
            if ch == in_str:
                in_str = None
        elif ch in "\"'":
            in_str = ch
        elif ch == open_ch:
            depth += 1
        elif ch == close_ch:
            depth -= 1
            if depth == 0:
                return i + 1
        i += 1
    return len(text)


def method_body_end(text, after_params):
    """Index just past a method's body (or past ``;`` for abstract methods)."""
    i = after_params
    while i < len(text):
        ch = text[i]
        if ch == "{":
            return balance(text, i, "{", "}")
        if ch == ";":
            return i + 1
        i += 1
    return len(text)


def parse_class_header(text):
    """Parse the top-level class declaration and its annotations."""
    m = CLASS_DECL_RE.search(text)
    if not m:
        return None
    class_name = m.group(1)
    header = text[: m.start()]
    # Only the tail after the last statement terminator can hold annotations.
    cut = header.rfind(";")
    tail = header[cut + 1 :] if cut >= 0 else header
    anns = OrderedDict()
    for name, args, _s, _e in iter_annotations(tail):
        anns[name] = parse_args(args)
    return {"class_name": class_name, "annotations": anns, "decl_start": m.start()}


def collect_annotations(segment):
    anns = OrderedDict()
    for name, args, _s, _e in iter_annotations(segment):
        anns[name] = parse_args(args)
    return anns


def method_annotations(segment):
    """Annotations that belong to the method: only those after the last ``;``."""
    cut = segment.rfind(";")
    tail = segment[cut + 1 :] if cut >= 0 else segment
    return collect_annotations(tail)


def mapping_of(anns):
    """Extract HTTP method + path + params condition from annotation map."""
    for ann_name, verb in MAPPING_ANN.items():
        if ann_name in anns:
            a = anns[ann_name] or OrderedDict()
            return verb, unquote(annotation_str(a, "value")), a
    if "RequestMapping" in anns:
        a = anns["RequestMapping"] or OrderedDict()
        path = unquote(annotation_str(a, "value"))
        verb = None
        if "method" in a:
            raw = a["method"]
            if isinstance(raw, list):
                raw = raw[0] if raw else ""
            m = re.search(r"RequestMethod\.(\w+)", str(raw))
            if m:
                verb = m.group(1).upper()
        return verb or "ANY", path, a
    return None


def permission_expressions(anns):
    """Human readable permission tokens from ``@PreAuthorize``.

    处理原系统实际出现的几种形态：
      * ``@pms.hasPermission('a','b')``            -> ``["a", "b"]``
      * ``@pms.hasPermissionOrSelf('a', #id)``     -> ``["a"]``（自持判定，忽略 #id）
      * ``@pms.isPlatformSuperAdmin()``            -> ``["isPlatformSuperAdmin"]``
      * ``permitAll()``                            -> ``[]``（显式放行，无权限要求）
    """
    out = []
    raw = anns.get("PreAuthorize")
    if raw is None:
        return out
    expr = annotation_str(raw, "value") or ""
    expr = expr.strip()
    if not expr:
        return out

    # 显式放行：视为无权限要求
    if re.fullmatch(r"permitAll\(\)\s*", expr):
        return out

    for m in re.finditer(r"hasPermission(?:OrSelf)?\(([^)]*)\)", expr):
        for tok in split_top_level(m.group(1)):
            tok = tok.strip().strip("'\"")
            # 跳过 SpEL 变量（如 #id）与空串
            if not tok or tok.startswith("#"):
                continue
            out.append(tok)

    if not out:
        # 无参判定方法：isPlatformSuperAdmin() / isAdmin() ...
        for m in re.finditer(r"\b([A-Za-z_]\w*)\s*\(\s*\)", expr):
            name = m.group(1)
            if name not in ("permitAll",):
                out.append(name)

    return out


def normalize_path(base, sub):
    parts = []
    for p in (base, sub):
        if not p:
            continue
        p = str(p).strip()
        if not p:
            continue
        parts.append(p.strip("/"))
    joined = "/".join(x for x in parts if x)
    return "/" + joined if joined else "/"


def as_path_list(value):
    """把注解里的路径参数统一成列表。

    ``"/a"`` -> ``["/a"]``；``{"/a", "/b"}`` -> ``["/a", "/b"]``
    （Spring 会把同一处理器注册到每个路径上）。
    """
    if value is None:
        return [""]
    if isinstance(value, list):
        return [v for v in value if v] or [""]
    return [value]


def parse_controller(path, module):
    text = read_text(path)
    text = strip_comments(text)
    header = parse_class_header(text)
    if header is None:
        return None, ["no class declaration"]
    anns = header["annotations"]
    class_req = anns.get("RequestMapping") or OrderedDict()
    # 类级路径也可能是数组：@RequestMapping({"/api/common/verification"})
    base_paths = as_path_list(unquote(annotation_str(class_req, "value")))
    base_path = base_paths[0] if base_paths else ""
    tag_ann = anns.get("Tag") or OrderedDict()
    tag_name = unquote(annotation_str(tag_ann, "name")) or ""
    tag_desc = unquote(annotation_str(tag_ann, "description")) or ""
    class_perms = permission_expressions(anns)
    class_preauth_raw = annotation_str(anns.get("PreAuthorize") or OrderedDict(), "value")
    class_validated = "Validated" in anns

    body = text[header["decl_start"] :]
    warnings = []
    endpoints = []

    pos = 0
    matches = list(METHOD_RE.finditer(body))
    consumed_to = 0
    for m in matches:
        if m.start() < consumed_to:
            continue  # inside a previous method body
        open_paren = body.index("(", m.end() - 1)
        after_params = balance(body, open_paren, "(", ")")
        end = method_body_end(body, after_params)
        consumed_to = end

        raw_params = body[open_paren + 1 : after_params - 1]
        segment = body[: m.start()]
        manns = method_annotations(segment)

        mapping = mapping_of(manns)
        if not mapping:
            continue
        verb, sub_path, map_args = mapping

        op_ann = manns.get("Operation") or OrderedDict()
        summary = unquote(annotation_str(op_ann, "summary")) or ""
        description = unquote(annotation_str(op_ann, "description")) or ""

        perms = permission_expressions(manns)
        preauth_raw = annotation_str(manns.get("PreAuthorize") or OrderedDict(), "value")

        params = []
        for raw in split_top_level(raw_params):
            info = parse_param(raw)
            if info is not None:
                params.append(info)

        ret = re.sub(r"\s+", " ", m.group("ret")).strip()

        # Spring 允许同一处理器映射到多个路径：@GetMapping({"/a","/b"})；
        # 类级 @RequestMapping 同样可以是数组，二者取笛卡尔积。
        sub_paths = as_path_list(sub_path)
        combos = [(b, s) for b in base_paths for s in sub_paths]

        for one_base, one_sub in combos:
            ep = OrderedDict()
            ep["module"] = module
            ep["controller"] = header["class_name"]
            ep["tag"] = tag_name
            ep["tag_description"] = tag_desc
            ep["method"] = verb
            ep["path"] = normalize_path(one_base, one_sub)
            ep["base_path"] = one_base
            ep["sub_path"] = one_sub or ""
            ep["operation"] = summary
            ep["description"] = description
            ep["permissions"] = perms if perms else list(class_perms)
            ep["permission_source"] = (
                "method" if perms else ("class" if class_perms else "none")
            )
            ep["preauthorize"] = preauth_raw or class_preauth_raw
            ep["java_method"] = m.group("name")
            ep["return_type"] = ret
            ep["params"] = params
            ep["mapping_params"] = (
                [unquote(x) for x in map_args["params"].strip("{}").split(",")]
                if map_args and "params" in map_args
                else []
            )
            ep["consumes"] = unquote(annotation_str(map_args or OrderedDict(), "consumes"))
            ep["produces"] = unquote(annotation_str(map_args or OrderedDict(), "produces"))
            ep["validated"] = class_validated or "Validated" in manns or "Valid" in manns
            ep["raw_response"] = WRAPPER_METHOD_ANN in manns
            ep["deprecated"] = "Deprecated" in manns
            ep["annotations"] = list(manns.keys())
            ep["source_file"] = os.path.relpath(path, JAVA_ROOT)
            ep["source_line"] = text[: header["decl_start"] + m.start()].count("\n") + 1

            # Convenience classification used by the coverage report.
            if ep["method"] == "ANY":
                warnings.append("%s: method-level @RequestMapping without verb" % ep["path"])
            if not summary:
                warnings.append("%s: missing @Operation(summary)" % ep["path"])

            endpoints.append(ep)

    return {
        "class_name": header["class_name"],
        "module": module,
        "base_path": base_path,
        "tag": tag_name,
        "class_permissions": class_perms,
        "source_file": os.path.relpath(path, JAVA_ROOT),
        "endpoints": endpoints,
    }, warnings


def module_for(path):
    p = path.replace("\\", "/")
    if "/tigshop-adminapi/" in p:
        return "adminapi"
    if "/tigshop-api/" in p:
        return "api"
    if "/tigshop-common/" in p:
        return "common"
    return "other"


def discover_controllers(root):
    found = []
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in ("target", ".git", "node_modules")]
        for fn in filenames:
            if not fn.endswith(".java"):
                continue
            full = os.path.join(dirpath, fn)
            try:
                text = read_text(full)
            except OSError:
                continue
            if "@RestController" in text or "@Controller" in text:
                found.append(full)
    return sorted(found)


# --------------------------------------------------------------------------
# Spring Security 登录端点
# --------------------------------------------------------------------------

# ``super.setFilterProcessesUrl("/api/user/login/signin");``
FILTER_URL_RE = re.compile(r"setFilterProcessesUrl\(\s*\"([^\"]+)\"\s*\)")


def parse_security_filters(root):
    """提取 Spring Security 过滤器注册的登录端点。

    登录不走 ``@RestController``，而是 ``UsernamePasswordAuthenticationFilter``
    子类通过 ``setFilterProcessesUrl`` 注册。只扫描控制器会漏掉它们，
    因此单独解析 ``tigshop-security`` 模块。
    """
    endpoints = []
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in ("target", ".git")]
        for fn in filenames:
            if not fn.endswith(".java"):
                continue
            full = os.path.join(dirpath, fn)
            text = read_text(full)
            if "setFilterProcessesUrl" not in text:
                continue
            m = FILTER_URL_RE.search(text)
            if not m:
                continue
            url = m.group(1)
            module = "adminapi" if url.startswith("/adminapi") else "api"
            cls = CLASS_DECL_RE.search(text)
            cls_name = cls.group(1) if cls else fn[:-5]
            doc = "登录" if "Login" in cls_name else "安全过滤器端点"
            if "ClientLoginFilter" in cls_name:
                doc = "会员登录（用户名/手机号 + 密码/验证码）"
            elif cls_name == "LoginFilter":
                doc = "管理后台登录（用户名/手机号 + 密码/验证码）"

            ep = OrderedDict()
            ep["module"] = module
            ep["controller"] = cls_name
            ep["tag"] = "登录"
            ep["tag_description"] = "Spring Security 过滤器注册的登录端点"
            ep["method"] = "POST"
            ep["path"] = url
            ep["base_path"] = url.rsplit("/", 1)[0]
            ep["sub_path"] = "/" + url.rsplit("/", 1)[-1]
            ep["operation"] = doc
            ep["description"] = "由 %s 的 setFilterProcessesUrl 注册" % cls_name
            ep["permissions"] = []
            ep["permission_source"] = "none"
            ep["preauthorize"] = None
            ep["java_method"] = "attemptAuthentication"
            ep["return_type"] = "AjaxResult<LoginVO>"
            ep["params"] = []
            ep["mapping_params"] = []
            ep["consumes"] = "application/json"
            ep["produces"] = None
            ep["validated"] = True
            ep["raw_response"] = False
            ep["deprecated"] = False
            ep["annotations"] = ["SpringSecurityFilter"]
            ep["source_file"] = os.path.relpath(full, JAVA_ROOT)
            ep["source_line"] = text[: m.start()].count("\n") + 1
            endpoints.append(
                {
                    "class_name": cls_name,
                    "module": module,
                    "base_path": ep["base_path"],
                    "tag": ep["tag"],
                    "class_permissions": [],
                    "source_file": ep["source_file"],
                    "endpoints": [ep],
                }
            )
    return endpoints


# --------------------------------------------------------------------------
# Reporting
# --------------------------------------------------------------------------

def build_inventory(java_root):
    global JAVA_ROOT
    JAVA_ROOT = java_root
    controllers = discover_controllers(java_root)
    parsed = []
    all_warnings = []
    for c in controllers:
        mod = module_for(c)
        if mod == "other":
            continue
        info, warns = parse_controller(c, mod)
        if info is None:
            all_warnings.append("%s: %s" % (os.path.relpath(c, java_root), warns))
            continue
        parsed.append(info)
        for w in warns:
            all_warnings.append("%s :: %s" % (info["class_name"], w))

    # Spring Security 登录过滤器（不走 @RestController，必须单独解析）
    security_root = os.path.join(java_root, "tigshop-security")
    if os.path.isdir(security_root):
        parsed.extend(parse_security_filters(security_root))

    return parsed, all_warnings


def flatten(parsed):
    rows = []
    for c in parsed:
        for ep in c["endpoints"]:
            rows.append(ep)
    return rows


def cmd_summary(parsed, warnings):
    rows = flatten(parsed)
    print("controllers parsed : %d" % len(parsed))
    print("endpoints          : %d" % len(rows))
    by_mod = defaultdict(int)
    by_verb = defaultdict(int)
    by_tag = defaultdict(int)
    for r in rows:
        by_mod[r["module"]] += 1
        by_verb[r["method"]] += 1
        by_tag[(r["module"], r["tag"])] += 1
    print("\nby module:")
    for k in sorted(by_mod):
        print("  %-10s %d" % (k, by_mod[k]))
    print("\nby verb:")
    for k in sorted(by_verb):
        print("  %-8s %d" % (k, by_verb[k]))
    documented = sum(1 for r in rows if r["operation"])
    print("\ndocumented (@Operation.summary): %d / %d (%.1f%%)" % (
        documented, len(rows), 100.0 * documented / max(1, len(rows))))
    guarded = sum(1 for r in rows if r["permissions"])
    print("with permission expression     : %d" % guarded)
    malformed = validate_paths(rows)
    print("malformed paths                : %d" % len(malformed))
    if malformed:
        print("\nmalformed path examples (解析失败，路径含表达式):")
        for m in malformed[:10]:
            print("  %r  %s" % (m["path"], m["source_file"]))
    dupes = defaultdict(list)
    for r in rows:
        dupes[(r["method"], r["path"])].append(r)
    dups = {k: v for k, v in dupes.items() if len(v) > 1}
    print("duplicate method+path pairs    : %d" % len(dups))
    print("warnings                       : %d" % len(warnings))
    if dups:
        print("\ntop duplicate method+path (Spring disambiguates by params=):")
        for k in sorted(dups)[:10]:
            print("  %-6s %-55s x%d" % (k[0], k[1], len(dups[k])))
    print("\ntop tags:")
    for k, v in sorted(by_tag.items(), key=lambda x: -x[1])[:25]:
        print("  %-9s %-22s %d" % (k[0], k[1] or "(untagged)", v))
    return rows


def write_json(parsed, out_dir):
    path = os.path.join(out_dir, "api-inventory.json")
    doc = OrderedDict()
    rows = flatten(parsed)
    doc["generated_by"] = "tools/java_api_parser.py"
    doc["source_root"] = JAVA_ROOT
    doc["controller_count"] = len(parsed)
    doc["endpoint_count"] = len(rows)
    doc["controllers"] = parsed
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(doc, fh, ensure_ascii=False, indent=2)
    return path


CSV_COLUMNS = [
    "module",
    "tag",
    "method",
    "path",
    "operation",
    "permissions",
    "auth",
    "java_method",
    "controller",
    "return_type",
    "param_count",
    "params",
    "raw_response",
    "source_file",
    "source_line",
]


def write_csv(parsed, out_dir, auth_map):
    path = os.path.join(out_dir, "api-inventory.csv")
    rows = flatten(parsed)
    with open(path, "w", encoding="utf-8-sig", newline="") as fh:
        w = csv.DictWriter(fh, fieldnames=CSV_COLUMNS, extrasaction="ignore")
        w.writeheader()
        for r in rows:
            params = "; ".join(
                "%s:%s%s"
                % (
                    p["wire_name"] or p["name"],
                    p["in"],
                    "" if p["required"] else "?",
                )
                for p in r["params"]
            )
            w.writerow(
                {
                    "module": r["module"],
                    "tag": r["tag"],
                    "method": r["method"],
                    "path": r["path"],
                    "operation": r["operation"],
                    "permissions": ",".join(r["permissions"]),
                    "auth": auth_map.get((r["module"], r["path"]), "unknown"),
                    "java_method": r["java_method"],
                    "controller": r["controller"],
                    "return_type": r["return_type"],
                    "param_count": len(r["params"]),
                    "params": params,
                    "raw_response": "yes" if r["raw_response"] else "",
                    "source_file": r["source_file"],
                    "source_line": r["source_line"],
                }
            )
    return path


def validate_paths(rows):
    """找出解析不干净的路径。

    路径里若残留 ``(`` ``)`` ``"`` ``+`` 等字符，说明注解里用了非字面量
    表达式（例如 ``@RequestMapping(("/api/user/account"))`` 或常量拼接），
    解析器没能还原真实路径。这类路径会让覆盖率比对出现假阴性，必须暴露出来。
    """
    bad = []
    for r in rows:
        p = r["path"]
        if any(ch in p for ch in '()"+\t ') or "//" in p or not p.startswith("/"):
            bad.append(r)
    return bad


def write_contract(parsed, out_dir, auth_map):
    """Compact contract embedded into the Rust binary for runtime docs.

    Only the fields needed to serve documentation and verify route coverage,
    which keeps the embedded payload small (a few hundred KB instead of MB).
    """
    rows = flatten(parsed)
    endpoints = []
    for r in rows:
        endpoints.append(
            OrderedDict(
                [
                    ("module", r["module"]),
                    ("method", r["method"]),
                    ("path", r["path"]),
                    ("summary", r["operation"]),
                    ("tag", r["tag"]),
                    ("auth", auth_map.get((r["module"], r["path"]), "unknown")),
                    ("permissions", r["permissions"]),
                    ("controller", r["controller"]),
                    ("handler", r["java_method"]),
                    ("source", "%s:%d" % (r["source_file"], r["source_line"])),
                ]
            )
        )
    doc = OrderedDict()
    doc["version"] = "5.8.57"
    doc["generated_by"] = "tools/java_api_parser.py"
    doc["endpoint_count"] = len(endpoints)
    doc["controller_count"] = len(parsed)
    doc["endpoints"] = endpoints
    path = os.path.join(out_dir, "api-contract.json")
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(doc, fh, ensure_ascii=False, separators=(",", ":"))
    return path


def write_markdown(parsed, out_dir, auth_map):
    rows = flatten(parsed)
    path = os.path.join(out_dir, "API-INVENTORY.md")
    by_mod_tag = OrderedDict()
    for r in rows:
        by_mod_tag.setdefault((r["module"], r["tag"] or "(untagged)"), []).append(r)

    lines = []
    lines.append("# TigShop API 清单（由 Java 源码自动提取）")
    lines.append("")
    lines.append("> 本文件由 `tools/java_api_parser.py` 从 Java 控制器的 Spring MVC 与")
    lines.append("> Swagger 注解直接生成，是 Rust 重写时的**接口契约基线**。")
    lines.append("> 请勿手工编辑；重新生成：`python tools/java_api_parser.py --all`。")
    lines.append("")
    lines.append("| 指标 | 数值 |")
    lines.append("|---|---|")
    lines.append("| 控制器 | %d |" % len(parsed))
    lines.append("| 接口总数 | %d |" % len(rows))
    documented = sum(1 for r in rows if r["operation"])
    lines.append("| 有 @Operation 文档 | %d |" % documented)
    lines.append("| 带权限表达式 | %d |" % sum(1 for r in rows if r["permissions"]))
    lines.append("")

    # Module overview
    lines.append("## 模块概览")
    lines.append("")
    lines.append("| 模块 | 接口数 | 说明 |")
    lines.append("|---|---|---|")
    mod_desc = {
        "adminapi": "管理后台 `/adminapi`",
        "api": "商城前台 `/api`",
        "common": "公共接口",
        "other": "其他",
    }
    by_mod = defaultdict(int)
    for r in rows:
        by_mod[r["module"]] += 1
    for k in sorted(by_mod):
        lines.append("| `%s` | %d | %s |" % (k, by_mod[k], mod_desc.get(k, "")))
    lines.append("")

    for (mod, tag), items in sorted(by_mod_tag.items()):
        lines.append("## %s / %s" % (mod, tag))
        lines.append("")
        lines.append("| 方法 | 路径 | 说明 | 权限 | 登录 |")
        lines.append("|---|---|---|---|---|")
        for r in sorted(items, key=lambda x: (x["path"], x["method"])):
            perm = ", ".join("`%s`" % p for p in r["permissions"]) or "-"
            auth = auth_map.get((r["module"], r["path"]), "?")
            lines.append(
                "| %s | `%s` | %s | %s | %s |"
                % (
                    r["method"],
                    r["path"],
                    (r["operation"] or "").replace("|", "\\|"),
                    perm,
                    auth,
                )
            )
        lines.append("")

    with open(path, "w", encoding="utf-8") as fh:
        fh.write("\n".join(lines))
    return path


OPENAPI_TYPE = {
    "String": {"type": "string"},
    "CharSequence": {"type": "string"},
    "Integer": {"type": "integer", "format": "int32"},
    "int": {"type": "integer", "format": "int32"},
    "Long": {"type": "integer", "format": "int64"},
    "long": {"type": "integer", "format": "int64"},
    "Boolean": {"type": "boolean"},
    "boolean": {"type": "boolean"},
    "BigDecimal": {"type": "number"},
    "Double": {"type": "number", "format": "double"},
    "double": {"type": "number", "format": "double"},
    "Float": {"type": "number", "format": "float"},
    "float": {"type": "number", "format": "float"},
    "MultipartFile": {"type": "string", "format": "binary"},
    "Date": {"type": "string", "format": "date-time"},
    "LocalDateTime": {"type": "string", "format": "date-time"},
    "LocalDate": {"type": "string", "format": "date"},
}


def write_openapi(parsed, out_dir, auth_map):
    rows = flatten(parsed)
    paths = OrderedDict()
    tags = OrderedDict()
    for r in rows:
        p = r["path"]
        entry = paths.setdefault(p, OrderedDict())
        verb = r["method"].lower()
        if verb == "any":
            verb = "post"
        if verb in entry:
            # Spring disambiguates duplicates via params=; keep the first and
            # record the collision for manual review.
            verb = verb + "x"
        if r["tag"] and r["tag"] not in tags:
            tags[r["tag"]] = {"name": r["tag"], "description": r["tag_description"]}

        params = []
        body_schema = None
        for prm in r["params"]:
            loc = prm["in"]
            base = prm["base_type"]
            schema = dict(OPENAPI_TYPE.get(base, {"type": "string"}))
            if loc == "body":
                body_schema = {
                    "type": "object",
                    "x-java-type": prm["java_type"],
                }
                continue
            if loc == "query_object":
                params.append(
                    {
                        "name": prm["name"],
                        "in": "query",
                        "required": False,
                        "style": "form",
                        "explode": True,
                        "schema": {"type": "object", "x-java-type": prm["java_type"]},
                        "description": "Spring 查询对象绑定（%s）" % prm["java_type"],
                    }
                )
                continue
            loc_map = {
                "query": "query",
                "path": "path",
                "header": "header",
                "cookie": "cookie",
                "file": "query",
            }
            entry_p = {
                "name": prm["wire_name"] or prm["name"],
                "in": loc_map.get(loc, "query"),
                "required": bool(prm["required"]) if loc != "path" else True,
                "schema": schema,
            }
            if loc == "file":
                entry_p["schema"] = {"type": "string", "format": "binary"}
            if prm["default"] is not None:
                entry_p["schema"] = dict(schema)
                entry_p["schema"]["default"] = prm["default"]
            params.append(entry_p)

        op = OrderedDict()
        op["tags"] = [r["tag"]] if r["tag"] else []
        op["summary"] = r["operation"] or r["java_method"]
        if r["description"]:
            op["description"] = r["description"]
        op["operationId"] = "%s_%s" % (r["controller"], r["java_method"])
        op["x-java-controller"] = "%s#%s" % (r["controller"], r["java_method"])
        op["x-java-source"] = "%s:%d" % (r["source_file"], r["source_line"])
        op["x-permissions"] = r["permissions"]
        op["x-auth"] = auth_map.get((r["module"], r["path"]), "unknown")
        op["x-response-wrapper"] = not r["raw_response"]
        op["x-return-type"] = r["return_type"]
        if params:
            op["parameters"] = params
        if body_schema:
            op["requestBody"] = {
                "required": True,
                "content": {"application/json": {"schema": body_schema}},
            }
        op["responses"] = {
            "200": {
                "description": "成功",
                "content": {
                    "application/json": {
                        "schema": {"$ref": "#/components/schemas/AjaxResult"}
                    }
                },
            }
        }
        if r["permissions"]:
            op["security"] = [{"bearerAuth": []}]
        entry[verb] = op

    spec = OrderedDict()
    spec["openapi"] = "3.1.0"
    spec["info"] = {
        "title": "TigShop B2B2C API",
        "version": "5.8.57",
        "description": (
            "由 Java 源码注解自动生成；Rust 重写实现的接口契约基线。"
            "响应统一包裹为 {code, msg, data}（除非 x-response-wrapper=false）。"
        ),
    }
    spec["servers"] = [
        {"url": "/adminapi", "description": "管理后台"},
        {"url": "/api", "description": "商城前台"},
    ]
    spec["tags"] = list(tags.values())
    spec["paths"] = paths
    spec["components"] = {
        "securitySchemes": {
            "bearerAuth": {"type": "http", "scheme": "bearer", "bearerFormat": "JWT"}
        },
        "schemas": {
            "AjaxResult": {
                "type": "object",
                "properties": {
                    "code": {"type": "integer"},
                    "msg": {"type": "string"},
                    "data": {},
                    "timestamp": {"type": "integer", "format": "int64"},
                },
            }
        },
    }
    path = os.path.join(out_dir, "openapi.json")
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(spec, fh, ensure_ascii=False, indent=2)
    return path


# --------------------------------------------------------------------------
# Auth inference from the security filters
# --------------------------------------------------------------------------

PERMIT_ALL_HINTS = (
    "login",
    "logout",
    "captcha",
    "register",
    "health",
    "swagger",
    "v3/api-docs",
    "error",
)


def infer_auth(rows, java_root):
    """Best-effort auth classification.

    Admin endpoints require JWT unless they are in the anonymous whitelist
    declared by ``LoginFilter``.  Storefront endpoints are mixed; the filter
    chain whitelists a set of public paths.
    """
    auth = {}
    for r in rows:
        path = r["path"]
        low = path.lower()
        if r["module"] == "adminapi":
            if any(h in low for h in PERMIT_ALL_HINTS):
                auth[(r["module"], path)] = "anonymous"
            else:
                auth[(r["module"], path)] = "jwt+perm" if r["permissions"] else "jwt"
        else:
            if any(h in low for h in PERMIT_ALL_HINTS):
                auth[(r["module"], path)] = "anonymous?"
            else:
                auth[(r["module"], path)] = "optional"
    return auth


# --------------------------------------------------------------------------
# Entry point
# --------------------------------------------------------------------------

JAVA_ROOT = ""


def main(argv=None):
    here = os.path.dirname(os.path.abspath(__file__))
    repo = os.path.dirname(here)
    default_java = os.path.join(os.path.dirname(repo), "java")
    default_out = os.path.join(repo, "docs")

    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--java-root", default=default_java)
    ap.add_argument("--out", default=default_out)
    ap.add_argument("--json", action="store_true")
    ap.add_argument("--csv", action="store_true")
    ap.add_argument("--md", action="store_true")
    ap.add_argument("--openapi", action="store_true")
    ap.add_argument("--contract", action="store_true", help="write api-contract.json")
    ap.add_argument("--all", action="store_true", help="write every artifact")
    ap.add_argument("--summary", action="store_true", help="print a summary")
    ap.add_argument(
        "--check",
        action="store_true",
        help="exit non-zero when undocumented endpoints exist",
    )
    args = ap.parse_args(argv)

    if not os.path.isdir(args.java_root):
        print("java root not found: %s" % args.java_root, file=sys.stderr)
        return 2

    parsed, warnings = build_inventory(args.java_root)
    rows = flatten(parsed)
    auth_map = infer_auth(rows, args.java_root)

    do_all = args.all or not any(
        [args.json, args.csv, args.md, args.openapi, args.contract, args.summary]
    )
    if do_all:
        os.makedirs(args.out, exist_ok=True)

    written = []
    if args.json or args.all:
        written.append(write_json(parsed, args.out))
    if args.csv or args.all:
        written.append(write_csv(parsed, args.out, auth_map))
    if args.md or args.all:
        written.append(write_markdown(parsed, args.out, auth_map))
    if args.openapi or args.all:
        written.append(write_openapi(parsed, args.out, auth_map))
    if args.contract or args.all:
        written.append(write_contract(parsed, args.out, auth_map))

    if args.summary or args.all:
        cmd_summary(parsed, warnings)

    for w in written:
        print("wrote %s" % os.path.relpath(w, os.getcwd()))

    if args.check:
        missing = [r for r in rows if not r["operation"]]
        bad_paths = validate_paths(rows)
        failed = False
        if missing:
            print(
                "\n%d endpoint(s) lack @Operation documentation" % len(missing),
                file=sys.stderr,
            )
            failed = True
        if bad_paths:
            print(
                "\n%d endpoint path(s) could not be resolved to a literal" % len(bad_paths),
                file=sys.stderr,
            )
            for b in bad_paths[:10]:
                print("  %r  %s" % (b["path"], b["source_file"]), file=sys.stderr)
            failed = True
        if failed:
            return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
