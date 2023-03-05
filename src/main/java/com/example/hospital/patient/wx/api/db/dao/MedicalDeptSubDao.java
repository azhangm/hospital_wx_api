package com.example.hospital.patient.wx.api.db.dao;

import java.util.List;
import java.util.Map;

public interface MedicalDeptSubDao {

    List<Map<Integer,String>> selectAllSubMedicalDeptSub(Integer deptId);

}




