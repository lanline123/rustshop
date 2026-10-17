import re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
t=open("crates/infrastructure/src/db/mod.rs",encoding="utf-8",errors="replace").read()
i=t.find("pub struct Repositories")
seg=t[i:t.find("\n}",i)]
fields=re.findall(r'pub\s+(\w+)\s*:\s*([\w:]+)',seg)
print("Repositories fields:",len(fields))
for n,ty in fields:
    if not re.match(r'slice\d+',n): print("  %-22s %s"%(n,ty))
print("\nnew() body sample:")
j=t.find("impl Repositories")
print(t[j:j+700])
