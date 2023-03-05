package com.example.hospital.patient.wx.api.service;

import java.util.List;
import java.util.Map;

public interface MedicalDeptSubService {


    List<Map<Integer,String>> selectAllMedicalSub(Integer medicalId);
}
