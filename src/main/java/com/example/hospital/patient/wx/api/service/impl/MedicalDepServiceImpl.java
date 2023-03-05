package com.example.hospital.patient.wx.api.service.impl;

import com.example.hospital.patient.wx.api.db.dao.MedicalDeptDao;
import com.example.hospital.patient.wx.api.service.MedicalDepService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class MedicalDepServiceImpl implements MedicalDepService {

    @Resource
    private MedicalDeptDao dao;


    @Override
    public List<Map<Integer,String>> selectAllMedicalDep(){
        return dao.selectAllMedicalDept();
    }


}
