package com.example.hospital.patient.wx.api.service.impl;

import cn.felord.payment.PayException;
import cn.felord.payment.wechat.v3.WechatApiProvider;
import cn.felord.payment.wechat.v3.WechatResponseEntity;
import cn.felord.payment.wechat.v3.model.Amount;
import cn.felord.payment.wechat.v3.model.PayParams;
import cn.felord.payment.wechat.v3.model.Payer;
import com.example.hospital.patient.wx.api.service.PaymentService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private WechatApiProvider wechatApiProvider;

    /**
     * 统一命令
     *
     * @param outTradeNo 贸易没有
     * @param openId     openid
     * @param total      总计
     * @param description    描述信息
     * @param notifyUrl  通知地址
     * @param timeExpire 时间到期
     * @return {@link ObjectNode}
     */
    public ObjectNode unifiedOrder(String outTradeNo,String openId,int total,String description , String notifyUrl , String timeExpire) {
        PayParams payParams = new PayParams();
        Payer payer = new Payer();
        payer.setOpenid(openId);
        payParams.setPayer(payer);
        Amount amount = new Amount();
//        1分钱
        amount.setTotal(1);

        payParams.setAmount(amount);
//        流水号
        payParams.setOutTradeNo(outTradeNo);

        payParams.setDescription(description);

        payParams.setNotifyUrl(notifyUrl);

        if (null != timeExpire) payParams.setTimeExpire(timeExpire);

        WechatResponseEntity<ObjectNode> response = wechatApiProvider.directPayApi("patient-wx-api").jsPay(payParams);

        if (response.is2xxSuccessful()) {
            return response.getBody();
        }else throw new PayException("创建微信支付订单失败");

    }

}
