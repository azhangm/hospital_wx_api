package com.example.hospital.patient.wx.api.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.hospital.patient.wx.api.db.dao.UserDao;
import com.example.hospital.patient.wx.api.db.dao.UserInfoCardDao;
import com.example.hospital.patient.wx.api.db.pojo.UserEntity;
import com.example.hospital.patient.wx.api.service.UserService;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserServiceImpl implements UserService {
    @Value("${wechat.app-id}")
    String appId;

    @Value("${wechat.app-secret}")
    String appSecret;

    @Resource
    private UserDao userDao;

    @Resource
    private UserInfoCardDao userInfoCardDao;

    @Override
    public Map<String,Object> loginOrRegistered (Map<String,Object> param) {
        Map<String,Object> map = new HashMap<>();

        String code = MapUtil.getStr(param, "code");
        // 由临时授权码获得 open_id
        String openId = getOpenId(code);

        Integer id = userDao.searchAlreadyRegistered(openId);
        if (id != null) map.put("successMessage","登录成功");
        else {
            String nickname = MapUtil.getStr(param, "nickname");
            String photo = MapUtil.getStr(param, "photo");
            String sex = MapUtil.getStr(param, "sex");
            UserEntity user = new UserEntity();
            user.setOpenId(openId);
            user.setNickname(nickname);
            user.setPhoto(photo);
            user.setSex(sex);
            user.setStatus((byte) 1);
            userDao.insert(user);
            id = userDao.searchAlreadyRegistered(user.getOpenId());
            map.put("successMessage","注册成功");
        }
        String phone = userInfoCardDao.searchUserTel(id);
        map.put("tel",phone);
        map.put("id",id);
        return map;
    }

    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String,Object> map = new HashMap<>();
        map.put("appid",appId);
        map.put("secret",appSecret);
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String response = HttpUtil.get(url,map);
        JSONObject json = JSONUtil.parseObj(response);
        String  openid = json.getStr("openid");
        if(openid == null || openid.length() == 0) throw new RuntimeException("临时登录凭证错误");
        return openid;
    }

}
