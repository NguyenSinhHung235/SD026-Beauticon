package com.example.vongtay.sales;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PosForm {
    private String requestToken;
    private Long customerId;
    private Long staffId;
    private String voucherCode;
    private String paymentMethod = "TIEN_MAT";
    private BigDecimal cashReceived;
    private boolean confirmedTransfer;
    private List<PosItem> items = new ArrayList<>();

    public String getRequestToken() { return requestToken; }
    public void setRequestToken(String v) { requestToken = v; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { customerId = v; }
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long v) { staffId = v; }
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String v) { voucherCode = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { paymentMethod = v; }
    public BigDecimal getCashReceived() { return cashReceived; }
    public void setCashReceived(BigDecimal v) { cashReceived = v; }
    public boolean isConfirmedTransfer() { return confirmedTransfer; }
    public void setConfirmedTransfer(boolean v) { confirmedTransfer = v; }
    public List<PosItem> getItems() { return items; }
    public void setItems(List<PosItem> v) { items = v == null ? new ArrayList<>() : v; }

    public static class PosItem {
        private Long variantId;
        private Integer quantity;
        public Long getVariantId() { return variantId; }
        public void setVariantId(Long v) { variantId = v; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer v) { quantity = v; }
    }
}
