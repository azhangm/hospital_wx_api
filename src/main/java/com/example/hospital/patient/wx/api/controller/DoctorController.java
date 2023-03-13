package com.example.hospital.patient.wx.api.controller;

import com.example.hospital.patient.wx.api.common.R;
import com.example.hospital.patient.wx.api.controller.form.SearchDoctorInfoByIdForm;
import com.example.hospital.patient.wx.api.service.DoctorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/doctor")
public class DoctorController {
    @Resource
    private DoctorService doctorService;

    @PostMapping("/searchDoctorInfoById")
    public R searchDoctorInfoById(@RequestBody @Valid SearchDoctorInfoByIdForm form) {
        return R.ok(doctorService.searchDoctorInfoById(form.getId()));
    }
}
