import subprocess,re,collections
out=subprocess.run(["git","grep","-l","-E",r"(^|[^:[:alnum:]_])slice[0-9]{3}::","HEAD","--","crates"],capture_output=True,text=True,cwd=r"D:\tigshop\java-b2b2b-pro-v5.8.57\rust").stdout
files=[l.split(":",1)[1] for l in out.splitlines()]
c=collections.Counter("/".join(f.split("/")[:2]) for f in files)
print(len(files),"files")
for k,v in c.most_common(): print(v,k)
print()
for f in files[:15]: print(f)
