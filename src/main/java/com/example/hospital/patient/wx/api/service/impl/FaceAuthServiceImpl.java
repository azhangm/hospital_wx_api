package com.example.hospital.patient.wx.api.service.impl;

import com.example.hospital.patient.wx.api.db.dao.FaceAuthDao;
import com.example.hospital.patient.wx.api.service.FaceAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
@Slf4j
public class FaceAuthServiceImpl implements FaceAuthService {

    @Resource
    private FaceAuthDao faceAuthDao;

    @Override
    public Boolean hasFaceAuthInDay(Map<String,Object> param) {
        Integer id = faceAuthDao.hasFaceAuthInDay(param);
        return id != null;
    }
}
