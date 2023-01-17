package com.example.hospital.patient.wx.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public interface UserService {
    Map<String,Object> loginOrRegistered(Map<String,Object> param);
}
