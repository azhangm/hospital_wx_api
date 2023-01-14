package com.example.hospital.patient.wx.api.service;

import java.util.Map;

public interface UserService {
    Map<String,Object> loginOrRegistered(Map<String,Object> param);
}
