import sys,importlib.util,os
sys.path.insert(0,"tools/restructure")
spec=importlib.util.spec_from_file_location("m","tools/restructure/migrate.py")
m=importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
w=m.build_world()
for k in ("slice012","slice001","slice027"):
    print(k, w.entries.get(k), "flat:",w.flat_var.get(k))
print("n entries:",len(w.entries))
# check bare alt correctness: simulate re
line="use crate::slice012::Slice012Service;"
print(m.re.sub(r"\b(slice\d{3})::", lambda mm: mm.group(1)+"::", line))
