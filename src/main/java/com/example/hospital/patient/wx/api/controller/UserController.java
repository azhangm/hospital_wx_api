package com.example.hospital.patient.wx.api.controller;


import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import com.example.hospital.patient.wx.api.common.R;
import com.example.hospital.patient.wx.api.controller.form.LoginOrRegisterForm;
import com.example.hospital.patient.wx.api.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService service;

    @PostMapping("/loginOrRegister")
    public R loginOrRegistered(@RequestBody @Valid LoginOrRegisterForm form) {
        Map<String , Object> map = new HashMap<>();
        map.put("code",form.getCode());
        map.put("nickname",form.getNickname());
        map.put("photo",form.getPhoto());
        map.put("sex",form.getSex());

        map = service.loginOrRegistered(map);
        String successMessage = MapUtil.getStr(map, "successMessage");
        String id = MapUtil.getStr(map, "id");
        StpUtil.login(id);
        String tokenValue = StpUtil.getTokenValue();
        return R.ok(successMessage).put("token",tokenValue);
    }
}
