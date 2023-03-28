package com.example.hospital.patient.wx.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.felord.payment.wechat.v3.WechatApiProvider;
import cn.felord.payment.wechat.v3.model.ResponseSignVerifyParams;
import cn.hutool.core.bean.BeanUtil;
import com.example.hospital.patient.wx.api.common.R;
import com.example.hospital.patient.wx.api.controller.form.*;
import com.example.hospital.patient.wx.api.service.RegistrationService;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/registration")
public class RegistrationController {
    @Resource
    private RegistrationService registrationService;

    @Resource
    private WechatApiProvider wechatApiProvider;

    @PostMapping("/searchCanRegisterInDateRange")
    @SaCheckLogin
    public R searchCanRegisterInDateRange(@RequestBody @Valid SearchCanRegisterInDateRangeForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        List<Map<String, Object>> maps = registrationService.searchCanRegisterInDateRange(param);
        return R.ok().put("result", maps);
    }

    @PostMapping("/searchDeptSubDoctorPlanInDay")
    public R searchDeptSubDoctorPlanInDay(@RequestBody @Valid SearchDeptSubDoctorPlanInDayForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        List<Map<String, Object>> maps = registrationService.searchDeptSubDoctorPlanInDay(param);
        return R.ok().put("result", maps);
    }

    @PostMapping("/checkRegisterCondition")
    @SaCheckLogin
    public R checkRegisterCondition(@RequestBody @Valid CheckRegisterConditionForm form) {
        int userId = StpUtil.getLoginIdAsInt();
        form.setUserId(userId);
        Map<String, Object> param = BeanUtil.beanToMap(form);
        String result = registrationService.checkRegisterCondition(param);
        return R.ok().put("result", result);
    }

    @PostMapping("/searchDoctorWorkPlanSchedule")
    @SaCheckLogin
    public R searchDoctorWorkPlanSchedule(@RequestBody @Valid SearchDoctorWorkPlanScheduleForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        return R.ok().put("result", registrationService.searchDoctorWorkPlanSchedule(param));
    }

    @PostMapping("/registerMedicalAppointment")
    @SaCheckLogin
    public R registerMedicalAppointment(@RequestBody @Valid RegisterMedicalAppointmentForm form) {
        int userId = StpUtil.getLoginIdAsInt();
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("userId", userId);
        Map<String, Object> result = registrationService.registerMedicalAppointment(param);
        if (result == null) {
            return R.ok();
        }
        return R.ok(result);
    }

    @SneakyThrows
    @PostMapping("/transaction-callback")
    public Map<String,String> transactionCallback(
            @RequestHeader("Wechatpay-Serial") String serial,
            @RequestHeader("Wechatpay-Signature") String signature,
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @RequestHeader("Wechatpay-Nonce") String nonce,
            HttpServletRequest request) {

        String body = request.getReader().lines().collect(Collectors.joining());

        //创建封装对象，添加相关参数，验证请求真伪需要用到
        ResponseSignVerifyParams params = new ResponseSignVerifyParams();
        params.setWechatpaySerial(serial);
        params.setWechatpaySignature(signature);
        params.setWechatpayTimestamp(timestamp);
        params.setWechatpayNonce(nonce);
        params.setBody(body);

        //验证通知消息的真伪
        return wechatApiProvider.callback("patient-wx-api").transactionCallback(params, data -> {
            String outTradeNo = data.getOutTradeNo();
            String transactionId = data.getTransactionId();
            //更新挂号记录的付款状态和付款单ID
            registrationService.updatePayment(new HashMap<>() {{
                put("outTradeNo", outTradeNo);
                put("transactionId", transactionId);
                put("paymentStatus", 2);
            }});

        });
    }
}

