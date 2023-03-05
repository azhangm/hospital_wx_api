package com.example.hospital.patient.wx.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.example.hospital.patient.wx.api.common.R;
import com.example.hospital.patient.wx.api.controller.form.InsertPatientUserInfoForm;
import com.example.hospital.patient.wx.api.controller.form.UpdateUserInfoCardForm;
import com.example.hospital.patient.wx.api.db.pojo.UserInfoCardEntity;
import com.example.hospital.patient.wx.api.service.UserInfoCardService;
import com.example.hospital.patient.wx.api.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/user/card/info")
public class UserInfoCardController {
///user/card/info/hasUserInfoCard
    @Resource
    private UserService userService;

    @Resource
    private UserInfoCardService userInfoCardService;
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
        @GetMapping("/searchUserInfoCard")
        @SaCheckLogin
        public R searchUserInfoCard() {
            int userId = StpUtil.getLoginIdAsInt();
            Map<String, Object> map = userInfoCardService.searchUserInfoCard(userId);
            if (MapUtil.isEmpty(map)) {
                return R.ok("没有查询到数据");
            }
            return R.ok(map);
        }

        @PostMapping("/update")
        @SaCheckLogin
        public R update(@RequestBody @Valid UpdateUserInfoCardForm form) {
            UserInfoCardEntity entity = BeanUtil.toBean(form, UserInfoCardEntity.class);
            String json = JSONUtil.parseArray(form.getMedicalHistory()).toString();
            entity.setMedicalHistory(json);
            userInfoCardService.update(entity);
            return R.ok();
        }


        @GetMapping("/hasUserInfoCard")
        @SaCheckLogin
        public R hasUserInfo() {
            int userId = StpUtil.getLoginIdAsInt();
            Boolean info = userInfoCardService.hasUserInfo(userId);
            return R.ok().put("result",info);
        }

}
