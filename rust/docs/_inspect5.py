import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

# scan all java files for @RestController / @Controller annotations
ann = []
for root in ['java']:
    for dirpath, dirnames, filenames in os.walk(root):
        if '\\test\\' in dirpath or '\\target\\' in dirpath:
            continue
        for fn in filenames:
            if not fn.endswith('.java'):
                continue
            p = os.path.join(dirpath, fn)
            try:
                with open(p, encoding='utf-8', errors='replace') as f:
                    txt = f.read()
            except Exception as e:
                print('ERR', p, e)
                continue
            if re.search(r'@(RestController|Controller)\b', txt):
                rel = os.path.relpath(p, 'java')
                cls = fn[:-5]
                ann.append((rel, cls, txt))

print('annotated controller classes:', len(ann))
from collections import Counter
print(Counter(a[0].split(os.sep)[0] for a in ann))

# classes annotated but filename not *Controller.java
odd = [a for a in ann if not a[1].endswith('Controller')]
print('annotated but not named *Controller:', len(odd))
for rel, cls, _ in odd:
    print('  ', rel)
