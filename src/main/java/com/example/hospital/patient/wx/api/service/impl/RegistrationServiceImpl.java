package com.example.hospital.patient.wx.api.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.example.hospital.patient.wx.api.db.dao.DoctorWorkPlanDao;
import com.example.hospital.patient.wx.api.service.RegistrationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistrationServiceImpl implements RegistrationService {
    @Resource
    private DoctorWorkPlanDao doctorWorkPlanDao;

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
}
