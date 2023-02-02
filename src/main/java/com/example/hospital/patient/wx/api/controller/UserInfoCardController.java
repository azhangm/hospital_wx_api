package com.example.hospital.patient.wx.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.example.hospital.patient.wx.api.common.R;
import com.example.hospital.patient.wx.api.controller.form.InsertPatientUserInfoForm;
import com.example.hospital.patient.wx.api.db.pojo.UserInfoCardEntity;
import com.example.hospital.patient.wx.api.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("user/card/info")
public class UserInfoCardController {

    @Resource
    private UserService userService;

    @SaCheckLogin
    @PostMapping("/insert")
    public R insertUserInfoCard(@RequestBody @Valid InsertPatientUserInfoForm form) {
        UserInfoCardEntity userInfoCardEntity = BeanUtil.toBean(form, UserInfoCardEntity.class);
        int userId = StpUtil.getLoginIdAsInt();
        userInfoCardEntity.setUserId(userId);
        String uuid = IdUtil.fastSimpleUUID().toUpperCase();
        userInfoCardEntity.setUuid(uuid);
        String json = JSONUtil.parseArray(form.getMedicalHistory()).toString();
        userInfoCardEntity.setMedicalHistory(json);
        userService.insertUserInfoCard(userInfoCardEntity);
        return R.ok();
    }
}
