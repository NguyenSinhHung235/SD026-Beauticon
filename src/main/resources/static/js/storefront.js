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
  // GD9: only offer sizing methods backed by actual sellable product variants.
  const sizeForm = document.querySelector('.bracelet-sizing-form');
  if (sizeForm) {
    const radios = Array.from(sizeForm.querySelectorAll('input[name="kieuKichThuoc"]'));
    const variantSelect = sizeForm.querySelector('#buy-variant');
    const sizeOptions = Array.from(variantSelect.options).filter(o => o.value !== '');
    const status = sizeForm.querySelector('#size-choice-message');
    const custom = sizeForm.querySelector('#custom-size-fields');
    const adjustable = sizeForm.querySelector('#free-range-panel');
    const button = sizeForm.querySelector('#buy-submit');
    const freeReady = Boolean(sizeForm.dataset.freeMin && sizeForm.dataset.freeMax);
    const isAvailable = mode => sizeOptions.some(o => o.dataset.sizeMode === mode)
      && (mode !== 'FREESIZE' || freeReady);

    radios.forEach(r => { r.disabled = !isAvailable(r.value); });
    const refresh = () => {
      const current = radios.find(r => r.checked && !r.disabled);
      const mode = current ? current.value : '';
      sizeOptions.forEach(option => {
        const visible = option.dataset.sizeMode === mode;
        option.hidden = !visible;
        option.disabled = !visible;
      });
      const chosen = variantSelect.selectedOptions[0];
      if (!chosen || chosen.disabled || (chosen.value !== '' && chosen.dataset.sizeMode !== mode))
        variantSelect.value = '';
      custom.hidden = mode !== 'THEO_YEU_CAU';
      adjustable.hidden = mode !== 'FREESIZE';
      custom.querySelectorAll('input,select,textarea').forEach(el => {
        el.disabled = mode !== 'THEO_YEU_CAU';
        if (el.id === 'wrist-cm' || el.id === 'fit-preference')
          el.required = mode === 'THEO_YEU_CAU';
      });
      if (button) button.disabled = !mode || sizeOptions.every(o => o.disabled);
      if (!mode) status.textContent = 'Sản phẩm chưa có phiên bản kích thước sẵn sàng để bán.';
      else if (mode === 'THEO_YEU_CAU') status.textContent = 'Nhập số đo cổ tay và độ rộng đeo. Shop sẽ xác nhận trước khi làm.';
      else if (mode === 'FREESIZE') status.textContent = `Phạm vi điều chỉnh: ${sizeForm.dataset.freeMin}–${sizeForm.dataset.freeMax} cm.`;
      else status.textContent = 'Chọn phiên bản màu sắc và size có sẵn phù hợp.';
    };
    if (!radios.some(r => r.checked && !r.disabled)) {
      const first = radios.find(r => !r.disabled);
      if (first) first.checked = true;
    }
    radios.forEach(r => r.addEventListener('change', refresh));
    variantSelect.addEventListener('change', () => {
      const chosen = variantSelect.selectedOptions[0];
      if (chosen && chosen.value && chosen.dataset.sizeMode) {
        const radio = radios.find(r => r.value === chosen.dataset.sizeMode && !r.disabled);
        if (radio && !radio.checked) { radio.checked = true; refresh(); }
      }
    });
    refresh();
  }

  const year = document.getElementById('footer-year');
  if (year) year.textContent = String(new Date().getFullYear());
  // Keep optional delivery fields out of the way when a saved address is selected.
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
  // Double-click guard is UX only; the server uses a unique checkout token.
  const checkout = document.getElementById('shop-checkout-form');
  if (checkout) checkout.addEventListener('submit', () => {
    const button = document.getElementById('shop-submit-order');
    if (button) {
      button.disabled = true;
      button.textContent = '\u0110ang g\u1eedi \u0111\u01a1n h\u00e0ng...';
    }
  });
});
