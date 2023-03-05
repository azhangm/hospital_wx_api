package com.example.hospital.patient.wx.api.db.dao;


import java.util.List;
import java.util.Map;

public interface MedicalDeptDao {
    List<Map<Integer,String>> selectAllMedicalDept();
}




