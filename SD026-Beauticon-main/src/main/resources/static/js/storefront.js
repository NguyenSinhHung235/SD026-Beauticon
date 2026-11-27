'use strict';
document.addEventListener('DOMContentLoaded', () => {
  const main = document.getElementById('detailMainPhoto');
  if (main) {
    document.querySelectorAll('.detail-gallery button[data-img-src]').forEach(button => {
      button.addEventListener('click', () => {
        main.src = button.dataset.imgSrc;
        main.alt = button.dataset.imgAlt || main.alt;
        document.querySelectorAll('.detail-gallery button').forEach(b => b.classList.remove('selected'));
        button.classList.add('selected');
      });
    });
  }

  const sizeForm = document.querySelector('.bracelet-sizing-form');
  if (sizeForm) {
    const radios = Array.from(sizeForm.querySelectorAll('input[name="kieuKichThuoc"]'));
    const variantSelect = sizeForm.querySelector('#buy-variant');
    const sizeOptions = Array.from(variantSelect.options).filter(o => o.value !== '');
    const status = sizeForm.querySelector('#size-choice-message');
    const custom = sizeForm.querySelector('#custom-size-fields');
    const adjustable = sizeForm.querySelector('#free-range-panel');
    const button = sizeForm.querySelector('#buy-submit');
    const wristInput = sizeForm.querySelector('#wrist-cm');
    const fitSelect = sizeForm.querySelector('#fit-preference');
    const calcGramEl = sizeForm.querySelector('#calc-cm-gram');
    const calcPriceEl = sizeForm.querySelector('#calc-cm-price');
    const livePriceEl = document.getElementById('detailLivePrice');

    const gramPerCmSetting = Number(sizeForm.dataset.gramPerCm || 0) || 0;
    const baseWeight = Number(sizeForm.dataset.baseWeight || 0) || 0;
    const materialRate = Number(sizeForm.dataset.materialRate || 0) || 0;
    const craftCost = Number(sizeForm.dataset.craftCost || 0) || 0;
    const freeReady = Boolean(sizeForm.dataset.freeMin && sizeForm.dataset.freeMax);
    const fmtMoney = (n) => Math.round(n).toLocaleString('vi-VN') + ' \u20ab';

    const fitDeltaCm = (fit) => {
      switch ((fit || '').toUpperCase()) {
        case 'OM_SAT':
        case 'SAT':
          return -0.5;
        case 'THOAI_MAI':
        case 'RONG':
          return 1.0;
        default:
          return 0.0;
      }
    };

    const updateCmPrice = (mode) => {
      const chosen = variantSelect.selectedOptions[0];
      const rawPrice = Number(chosen?.dataset?.rawPrice || 0) || 0;
      const surcharge = Number(chosen?.dataset?.surcharge || 0) || 0;

      if (mode !== 'THEO_YEU_CAU') {
        if (chosen && chosen.value && rawPrice > 0 && livePriceEl) {
          livePriceEl.textContent = fmtMoney(rawPrice);
        }
        return;
      }

      const rawCm = Number(wristInput?.value || 0);
      if (!Number.isFinite(rawCm) || rawCm <= 0) {
        if (calcGramEl) calcGramEl.textContent = '0 g';
        if (calcPriceEl) calcPriceEl.textContent = fmtMoney(rawPrice > 0 ? rawPrice : craftCost + surcharge);
        return;
      }

      const effectiveCm = Math.max(1, rawCm + fitDeltaCm(fitSelect?.value));
      const gramPerCm = gramPerCmSetting > 0 ? gramPerCmSetting : (baseWeight > 0 ? (baseWeight / 16) : 1);
      const estGram = Math.round(effectiveCm * gramPerCm * 100) / 100;
      const estPrice = materialRate > 0
          ? Math.max(0, Math.round(estGram * materialRate + craftCost + surcharge))
          : Math.max(0, Math.round((rawPrice > 0 ? rawPrice : craftCost) + surcharge));

      if (calcGramEl) calcGramEl.textContent = estGram.toLocaleString('vi-VN') + ' g';
      if (calcPriceEl) calcPriceEl.textContent = fmtMoney(estPrice);
      if (livePriceEl) livePriceEl.textContent = fmtMoney(estPrice);
    };

    const hasAnyStockVariant = sizeOptions.length > 0;
    const isAvailable = mode => {
      if (!hasAnyStockVariant) return false;
      if (mode === 'THEO_YEU_CAU') return true;
      if (mode === 'FREESIZE') return freeReady && sizeOptions.some(o => o.dataset.sizeMode === 'FREESIZE');
      return sizeOptions.some(o => o.dataset.sizeMode === mode);
    };

    radios.forEach(r => { r.disabled = !isAvailable(r.value); });
    const refresh = () => {
      const current = radios.find(r => r.checked && !r.disabled);
      const mode = current ? current.value : '';
      sizeOptions.forEach(option => {
        const visible = (mode === 'THEO_YEU_CAU') ? true : (option.dataset.sizeMode === mode);
        option.hidden = !visible;
        option.disabled = !visible;
      });
      const chosen = variantSelect.selectedOptions[0];
      if (!chosen || chosen.disabled || (chosen.value !== '' && mode !== 'THEO_YEU_CAU' && chosen.dataset.sizeMode !== mode))
        variantSelect.value = '';
      custom.hidden = mode !== 'THEO_YEU_CAU';
      adjustable.hidden = mode !== 'FREESIZE';
      custom.querySelectorAll('input,select,textarea').forEach(el => {
        el.disabled = mode !== 'THEO_YEU_CAU';
        if (el.id === 'wrist-cm' || el.id === 'fit-preference')
          el.required = mode === 'THEO_YEU_CAU';
      });
      if (button) button.disabled = !mode || !hasAnyStockVariant;
      if (!mode) status.textContent = 'Sản phẩm chưa có phiên bản kích thước sẵn sàng để bán.';
      else if (mode === 'THEO_YEU_CAU') status.textContent = 'Chọn màu/mẫu nền và nhập số đo cổ tay (cm) để tính gram & giá.';
      else if (mode === 'FREESIZE') status.textContent = `Phạm vi điều chỉnh: ${sizeForm.dataset.freeMin}–${sizeForm.dataset.freeMax} cm.`;
      else status.textContent = 'Chọn phiên bản màu sắc và size có sẵn phù hợp.';
      updateCmPrice(mode);
    };

    if (!radios.some(r => r.checked && !r.disabled)) {
      const first = radios.find(r => !r.disabled);
      if (first) first.checked = true;
    }
    radios.forEach(r => r.addEventListener('change', refresh));
    variantSelect.addEventListener('change', () => {
      const currentMode = radios.find(r => r.checked && !r.disabled)?.value;
      const chosen = variantSelect.selectedOptions[0];
      if (currentMode !== 'THEO_YEU_CAU' && chosen && chosen.value && chosen.dataset.sizeMode) {
        const radio = radios.find(r => r.value === chosen.dataset.sizeMode && !r.disabled);
        if (radio && !radio.checked) { radio.checked = true; }
      }
      refresh();
    });
    if (wristInput) {
      wristInput.addEventListener('input', () => {
        const current = radios.find(r => r.checked && !r.disabled);
        updateCmPrice(current ? current.value : '');
      });
      wristInput.addEventListener('change', () => {
        const current = radios.find(r => r.checked && !r.disabled);
        updateCmPrice(current ? current.value : '');
      });
    }
    if (fitSelect) {
      fitSelect.addEventListener('change', () => {
        const current = radios.find(r => r.checked && !r.disabled);
        updateCmPrice(current ? current.value : '');
      });
    }
    refresh();
  }

  const year = document.getElementById('footer-year');
  if (year) year.textContent = String(new Date().getFullYear());
  const addressOptions = document.querySelectorAll('input[name="idDiaChi"]');
  const newAddress = document.querySelector('.new-address-fields');
  if (addressOptions.length && newAddress) {
    const refreshAddress = () => {
      const selected = document.querySelector('input[name="idDiaChi"]:checked');
      const useNew = !selected || selected.value === '';
      newAddress.hidden = !useNew;
      newAddress.querySelectorAll('input,textarea').forEach(field => {
        field.required = useNew;
        field.disabled = !useNew;
      });
    };
    addressOptions.forEach(option => option.addEventListener('change', refreshAddress));
    refreshAddress();
  }
  const checkout = document.getElementById('shop-checkout-form');
  if (checkout) checkout.addEventListener('submit', () => {
    const button = document.getElementById('shop-submit-order');
    if (button) {
      button.disabled = true;
      button.textContent = '\u0110ang g\u1eedi \u0111\u01a1n h\u00e0ng...';
    }
  });
});