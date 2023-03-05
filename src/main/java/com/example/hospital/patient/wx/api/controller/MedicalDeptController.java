package com.example.hospital.patient.wx.api.controller;

import com.example.hospital.patient.wx.api.common.R;
import com.example.hospital.patient.wx.api.service.MedicalDepService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medical/dept")
public class MedicalDeptController {
    @Resource
    private MedicalDepService medicalDeptService;

    @PostMapping("/searchMedicalDeptList")
    public R searchMedicalDeptList() {
        List<Map<Integer, String>> list = medicalDeptService.selectAllMedicalDep();
        return R.ok().put("result", list);
    }
}
