package com.example.hospital.patient.wx.api.service.impl;

import com.example.hospital.patient.wx.api.db.dao.DoctorDao;
import com.example.hospital.patient.wx.api.service.DoctorService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Resource
    private DoctorDao dao;

    @Override
    public Map<String, Object> searchDoctorInfoById(Integer id) {
        return dao.searchDoctorInfoById(id);
    }
}
