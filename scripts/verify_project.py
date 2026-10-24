#!/usr/bin/env python3
"""Dependency-free structural smoke checks. This does not replace Maven or SQL Server tests."""
from __future__ import annotations
import re
import sys
from html.parser import HTMLParser
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'src/main'
TEMPLATES = SRC / 'resources/templates'
SQL = ROOT / 'sql/01_schema.sql'
errors=[]
notes=[]

def check(condition, msg):
    if not condition: errors.append(msg)

class HTMLCheck(HTMLParser):
    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.forms=0
        self.nested_form=False
        self.ids=[]
        self.buttons=[]
        self.assets=[]
    def handle_starttag(self,tag, attrs):
        a=dict(attrs)
        if tag=='form':
            if self.forms: self.nested_form=True
            self.forms+=1
        if tag=='input' and a.get('type')=='image':
            check(bool(a.get('alt')), 'image input without alt')
        if 'id' in a: self.ids.append(a['id'])
        if tag=='img' and not a.get('alt'): self.buttons.append('img missing alt')
        if tag=='link' and 'th:href' in a: self.assets.append(a['th:href'])
    def handle_endtag(self,tag):
        if tag=='form': self.forms=max(0,self.forms-1)

java='\n'.join(p.read_text(encoding='utf8') for p in (SRC/'java').rglob('*.java'))
schema=SQL.read_text(encoding='utf8')
tables={t.lower() for t in re.findall(r'CREATE TABLE\s+dbo\.([A-Za-z0-9_]+)',schema,re.I)}
check(len(tables)==22,f'expected 22 tables, found {len(tables)}')
constraints=re.findall(r'\bCONSTRAINT\s+([A-Za-z0-9_]+)',schema,re.I)
check(len(constraints)==len(set(c.lower() for c in constraints)),'duplicate named constraint in schema')
for tab in re.findall(r'\bdbo\.([A-Za-z0-9_]+)',java,re.I):
    check(tab.lower() in tables, f'Java references unknown SQL table: {tab}')

mappings=[]
for f in (SRC/'java').rglob('*Controller.java'):
    s=f.read_text(encoding='utf8')
    cm=re.search(r'@RequestMapping\("([^"]+)"\)\s*public\s+class',s)
    pref=cm.group(1) if cm else ''
    for meth in re.finditer(r'@(GetMapping|PostMapping)\s*(?:\(([^)]*)\))?',s):
        typ=meth.group(1).upper().replace('MAPPING','')
        expr=meth.group(2) or ''
        paths=re.findall(r'"([^"]+)"',expr) or ['']
        for p in paths:
            route=pref+p if p.startswith('/') or not p else pref+'/'+p
            if p=='': route=pref
            mappings.append((typ,route))
for f in (SRC/'java').rglob('*.java'):
    s=f.read_text(encoding='utf8')
    if f.name.endswith('Controller.java'):
        for return_template in re.findall(r'return\s+"(admin/[A-Za-z0-9/_-]+|auth/[A-Za-z0-9/_-]+)"',s):
            check((TEMPLATES/(return_template+'.html')).exists(),f'{f.name}: missing template {return_template}.html')

def is_match(uri,method):
    if uri.startswith(('/css/','/js/','/images/','/uploads/')):
        if uri.startswith('/css/'): return (SRC/'resources/static'/uri.lstrip('/')).exists()
        return True
    if uri in ['/login','/logout']:
        return True
    for m,route in mappings:
        if m!=method: continue
        r='^'+re.sub(r'\\\{[^}]+\\\}',r'[^/]+',re.escape(route))+'/?$'
        if re.fullmatch(r,uri): return True
    return False

count_routes=0
for html_file in sorted(TEMPLATES.rglob('*.html')):
    data=html_file.read_text(encoding='utf8')
    parser=HTMLCheck();parser.feed(data)
    check(not parser.nested_form,f'{html_file.relative_to(ROOT)} nested form')
    check(len(set(parser.ids))==len(parser.ids),f'{html_file.name}: duplicate static element id')
    check(not parser.buttons,f'{html_file.name}: '+','.join(parser.buttons))
    for src in parser.assets:
        if src.startswith('@{/css/'):
            check((SRC/'resources/static'/src[3:-1]).exists(),f'{html_file.name}: CSS file missing {src}')
    for frag in re.findall(r'~\{([\w/-]+)\s*::',data):
        check((TEMPLATES/(frag+'.html')).exists(),f'{html_file.name}: missing fragment {frag}')
    for m in re.finditer(r'@\{',data):
        i=m.end(); depth=1; start=i
        while i<len(data) and depth:
            if data[i]=='{': depth+=1
            elif data[i]=='}': depth-=1
            i+=1
        expr=data[start:i-1]
        if not expr.startswith('/'): continue
        uri=expr.split('(',1)[0]
        # Static URL only: for placeholders, try substitute an ID
        uri=re.sub(r'\{[^{}]+\}','123',uri)
        # Infer the action method from the actual containing element. Never
        # treat an unhandled GET link as valid merely because a POST exists.
        tag_prefix=data[data.rfind('<',0,m.start()):m.start()]
        if 'data-quote-url=' in tag_prefix:
            method='POST'
        elif 'th:action=' in tag_prefix and tag_prefix.lstrip().startswith('<form'):
            method='POST' if re.search(r'method=["\']post["\']',tag_prefix,re.I) else 'GET'
        else:
            method='GET'
        if not is_match(uri,method):
            errors.append(f'{html_file.name}: unhandled {method} URL {uri}')
        count_routes+=1

check('admin/fragments/nav :: menu(\'dashboard\')' in (TEMPLATES/'admin/dashboard.html').read_text(),
      'Dashboard must use shared nav fragment')
check('signedInStaff' in (TEMPLATES/'admin/fragments/nav.html').read_text(),
      'Shared nav must include signed-in staff')
check('hasRole("ADMIN")' in (SRC/'java/com/example/vongtay/security/SecurityConfig.java').read_text(),
      'No Admin role checks in security config')
check('idempotent' not in schema.lower(), 'check statement')

print(f'SQL tables: {len(tables)}, route declarations: {len(mappings)}, URLs checked: {count_routes}, templates: {len(list(TEMPLATES.rglob("*.html")))}')
if errors:
    for msg in dict.fromkeys(errors): print('ERROR:',msg)
    sys.exit(1)
print('PASS: static schema/template/controller/asset checks (NOT an integration test)')
