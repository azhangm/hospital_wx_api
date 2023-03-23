package com.example.hospital.patient.wx.api.db.dao;

import java.util.List;
import java.util.Map;

public interface DoctorWorkPlanScheduleDao {

     List<Map<String,Object>> searchDoctorWorkPlanSchedule(Map param);


     int updateNumById(Map<String,Object> param);

}




