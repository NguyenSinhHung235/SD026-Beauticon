#!/usr/bin/env python3
"""Browser behavior check for the actual Thymeleaf sizing form with fixture variants."""
from pathlib import Path
from playwright.sync_api import sync_playwright
import re

root=Path(__file__).resolve().parent.parent
script=(root/'src/main/resources/static/js/storefront.js').read_text()
css=(root/'src/main/resources/static/css/storefront.css').read_text()
template=(root/'src/main/resources/templates/shop/detail.html').read_text()
form=re.search(r'<form class="detail-add-form[\s\S]*?</form>',template).group(0)
form=form.replace('th:attr="data-free-min=${product.freesizeMinCm},data-free-max=${product.freesizeMaxCm}"', 'data-free-min="14.0" data-free-max="20.0"')
form=re.sub(r'<option th:each="v : \$\{product\.variants\}"[\s\S]*?</option>',
'''<option value="101" data-size-mode="CO_SAN">Màu bạc / 16 cm</option>
<option value="102" data-size-mode="FREESIZE">Màu bạc / Freesize</option>
<option value="103" data-size-mode="THEO_YEU_CAU">Màu bạc / Theo yêu cầu</option>''',form)
html=f'<!doctype html><html lang="vi"><head><meta charset="UTF-8"><style>{css}</style></head><body><main class="detail-container" style="max-width:690px"><h2>Vòng tay Ánh Sao – chọn kích thước</h2>{form}</main></body></html>'

def check(cond,msg):
    if not cond: raise AssertionError(msg)

with sync_playwright() as p:
    browser=p.chromium.launch(headless=True,executable_path='/usr/bin/chromium',args=['--no-sandbox'])
    try:
        page=browser.new_page(viewport={'width':1000,'height':850},device_scale_factor=1)
        page.set_content(html)
        page.add_script_tag(content=script)
        page.evaluate("document.dispatchEvent(new Event('DOMContentLoaded'))")
        check(page.locator('input[name=kieuKichThuoc]:checked').input_value()=='CO_SAN','Default fixed')
        check(page.locator('#custom-size-fields').is_hidden(),'Custom should be hidden')
        check(page.locator('#buy-variant option[value="103"]').is_disabled(),'Custom option filtered')
        page.locator('input[value="THEO_YEU_CAU"]').check()
        check(page.locator('#custom-size-fields').is_visible(),'Custom fields should be shown')
        check(page.locator('#wrist-cm').is_enabled(),'Wrist field must be enabled')
        check(page.locator('#wrist-cm').get_attribute('required') is not None,'Wrist required')
        page.locator('#buy-variant').select_option('103')
        page.locator('#wrist-cm').fill('16.5')
        page.locator('#fit-preference').select_option('VUA')
        check(page.evaluate('document.querySelector(".bracelet-sizing-form").checkValidity()'),'Valid custom form')
        page.screenshot(path=str(root/'demo/screenshots/05_product_sizing_GD9.png'),full_page=True)
        page.locator('input[value="FREESIZE"]').check()
        check(page.locator('#free-range-panel').is_visible(),'Free range shown')
        check(page.locator('#wrist-cm').is_disabled(),'Wrist field should be disabled after switch')
        check(page.locator('#buy-variant option[value="101"]').is_disabled(),'Fixed variant filtered')
        check(not page.locator('#buy-variant option[value="102"]').is_disabled(),'Free variant enabled')
        page.locator('input[value="CO_SAN"]').check()
        check(page.locator('#custom-size-fields').is_hidden(),'Custom fields hide when returning fixed')
        no_range=browser.new_page()
        no_range.set_content(html.replace('data-free-min="14.0" data-free-max="20.0"',''))
        no_range.add_script_tag(content=script)
        no_range.evaluate("document.dispatchEvent(new Event('DOMContentLoaded'))")
        check(no_range.locator('input[value="FREESIZE"]').is_disabled(),'Free cannot be sold without configured range')
        print('PASS: browser sizing selection, custom measurement validation, free-range guard, variant filtering.')
    finally:
        browser.close()
