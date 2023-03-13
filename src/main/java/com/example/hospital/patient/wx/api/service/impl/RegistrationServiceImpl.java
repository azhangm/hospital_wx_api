package com.example.hospital.patient.wx.api.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.example.hospital.patient.wx.api.db.dao.DoctorWorkPlanDao;
import com.example.hospital.patient.wx.api.db.dao.DoctorWorkPlanScheduleDao;
import com.example.hospital.patient.wx.api.db.dao.MedicalRegistrationDao;
import com.example.hospital.patient.wx.api.db.dao.UserInfoCardDao;
import com.example.hospital.patient.wx.api.service.FaceAuthService;
import com.example.hospital.patient.wx.api.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {
    @Resource
    private DoctorWorkPlanDao doctorWorkPlanDao;

    @Resource
    private MedicalRegistrationDao medicalRegistrationDao;

    @Resource
    private UserInfoCardDao userInfoCardDao;

    @Resource
    private FaceAuthService faceAuthService;

    @Resource
    private DoctorWorkPlanScheduleDao dao;

    @Override
    public List<Map<String, Object>> searchCanRegisterInDateRange(Map<String,Object> param) {
        List<String> list = doctorWorkPlanDao.searchCanRegisterInDateRange(param);
        DateTime startDate = DateUtil.parse(MapUtil.getStr(param, "startDate"));
        DateTime endDate = DateUtil.parse(MapUtil.getStr(param, "endDate"));

        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        List<Map<String,Object>> result = new ArrayList<>();
        while (range.hasNext()) {
            String date = range.next().toDateStr();
            if (list.contains(date)) {
                result.add(new HashMap<>() {{
                    put("date", date);
                    put("status", "出诊");
                }});
            } else {
                result.add(new HashMap<>() {{
                    put("date", date);
                    put("status", "无号");

                }});
            }
        }
        return result;
    }

    @Override
    public List<Map<String,Object>> searchDeptSubDoctorPlanInDay(Map<String,Object> param) {
        return doctorWorkPlanDao.searchDeptSubDoctorPlanInDay(param);
    }

    
    @Override
    public String checkRegisterCondition(Map<String,Object> param) {
        //检查当天用户是否已经挂号3次以上
        param.put("today", DateUtil.today());
        long count = medicalRegistrationDao.searchRegistrationCountInToday(param);
        if (count == 3) {
            return "已经达到当天挂号上限";
        }

        //检查当天是否已经挂过该门诊的号
        Integer id = medicalRegistrationDao.hasRegisterRecordInDay(param);
        if (id != null) {
            return "已经挂过该诊室的号";
        }

        //检查是否存在人脸面部数据
//        int userId = MapUtil.getInt(param, "userId");

//        Boolean existFaceModel = userInfoCardDao.searchExistFaceModel(userId);
//        log.error("Boolean  ----- " , existFaceModel);
//        if (null == existFaceModel || !existFaceModel) {
//            return "不存在面部模型";
//        }
        //检查今日是否存在挂号用户的面部识别记录
        Boolean flag = faceAuthService.hasFaceAuthInDay(param);
        if (!flag) {
            return "当日没有人脸验证记录";
        }
        return "满足挂号条件";
    }

    @Override
    public List<Map<String, Object>> searchDoctorWorkPlanSchedule(Map<String, Object> param) {
        return dao.searchDoctorWorkPlanSchedule(param);
    }
}
