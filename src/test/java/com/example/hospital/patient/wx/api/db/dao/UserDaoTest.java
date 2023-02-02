package com.example.hospital.patient.wx.api.db.dao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserDaoTest {
    @Resource
    private UserDao userDao;

    @Test
    public void testSearch() {
        Map<String, Object> map = userDao.searchUserInfo(1);
        System.out.println(map);
    }
}