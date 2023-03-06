package com.example.hospital.patient.wx.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.bean.BeanUtil;
import com.example.hospital.patient.wx.api.common.R;
import com.example.hospital.patient.wx.api.controller.form.SearchCanRegisterInDateRangeForm;
import com.example.hospital.patient.wx.api.controller.form.SearchDeptSubDoctorPlanInDayForm;
import com.example.hospital.patient.wx.api.service.RegistrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/registration")
public class RegistrationController {
    @Resource
    private RegistrationService registrationService;

    @PostMapping("/searchCanRegisterInDateRange")
    @SaCheckLogin
    public R searchCanRegisterInDateRange(@RequestBody @Valid SearchCanRegisterInDateRangeForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        List<Map<String, Object>> maps = registrationService.searchCanRegisterInDateRange(param);
        return R.ok().put("result", maps);
    }

    @PostMapping("/searchDeptSubDoctorPlanInDay")
    @SaCheckLogin
    public R searchDeptSubDoctorPlanInDay(@RequestBody @Valid SearchDeptSubDoctorPlanInDayForm form) {
        Map<String,Object> param = BeanUtil.beanToMap(form);
        List<Map<String, Object>> maps = registrationService.searchDeptSubDoctorPlanInDay(param);
        return R.ok().put("result", maps);
    }
}
