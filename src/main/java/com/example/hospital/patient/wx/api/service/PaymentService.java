package com.example.hospital.patient.wx.api.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface PaymentService {
    ObjectNode unifiedOrder(String outTradeNo, String openId, int total, String description, String notifyUrl, String timeExpire);
}
