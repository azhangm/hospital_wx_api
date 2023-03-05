package com.example.hospital.patient.wx.api.service.impl;

import com.example.hospital.patient.wx.api.db.dao.MedicalDeptSubDao;
import com.example.hospital.patient.wx.api.service.MedicalDeptSubService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class MedicalDeptSubServiceImpl implements MedicalDeptSubService {

    @Resource
    private MedicalDeptSubDao dao;

    @Override
    public List<Map<Integer,String>> selectAllMedicalSub(Integer medicalId){
        return dao.selectAllSubMedicalDeptSub(medicalId);
    }
}
