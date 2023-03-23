package com.example.hospital.patient.wx.api.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.example.hospital.patient.wx.api.db.dao.*;
import com.example.hospital.patient.wx.api.service.FaceAuthService;
import com.example.hospital.patient.wx.api.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {
    @Resource
    private DoctorWorkPlanDao doctorWorkPlanDao;

    @Resource
    private MedicalRegistrationDao medicalRegistrationDao;

    @Resource
    private UserInfoCardDao userInfoCardDao;

    @Resource
    private FaceAuthService faceAuthService;

    @Resource
    private DoctorWorkPlanScheduleDao dao;

    @Resource
    private UserDao userDao;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public List<Map<String, Object>> searchCanRegisterInDateRange(Map<String,Object> param) {
        List<String> list = doctorWorkPlanDao.searchCanRegisterInDateRange(param);
        DateTime startDate = DateUtil.parse(MapUtil.getStr(param, "startDate"));
        DateTime endDate = DateUtil.parse(MapUtil.getStr(param, "endDate"));

        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        List<Map<String,Object>> result = new ArrayList<>();
        while (range.hasNext()) {
            String date = range.next().toDateStr();
            if (list.contains(date)) {
                result.add(new HashMap<>() {{
                    put("date", date);
                    put("status", "出诊");
                }});
            } else {
                result.add(new HashMap<>() {{
                    put("date", date);
                    put("status", "无号");

                }});
            }
        }
        return result;
    }

    @Override
    public List<Map<String,Object>> searchDeptSubDoctorPlanInDay(Map<String,Object> param) {
        return doctorWorkPlanDao.searchDeptSubDoctorPlanInDay(param);
    }

    
    @Override
    public String checkRegisterCondition(Map<String,Object> param) {
        //检查当天用户是否已经挂号3次以上
        param.put("today", DateUtil.today());
        long count = medicalRegistrationDao.searchRegistrationCountInToday(param);
        if (count == 3) {
            return "已经达到当天挂号上限";
        }

        //检查当天是否已经挂过该门诊的号
        Integer id = medicalRegistrationDao.hasRegisterRecordInDay(param);
        if (id != null) {
            return "已经挂过该诊室的号";
        }

        //检查是否存在人脸面部数据
//        int userId = MapUtil.getInt(param, "userId");

//        Boolean existFaceModel = userInfoCardDao.searchExistFaceModel(userId);
//        log.error("Boolean  ----- " , existFaceModel);
//        if (null == existFaceModel || !existFaceModel) {
//            return "不存在面部模型";
//        }
        //检查今日是否存在挂号用户的面部识别记录
        Boolean flag = faceAuthService.hasFaceAuthInDay(param);
        if (!flag) {
            return "当日没有人脸验证记录";
        }
        return "满足挂号条件";
    }

    @Override
    public List<Map<String, Object>> searchDoctorWorkPlanSchedule(Map<String, Object> param) {
        return dao.searchDoctorWorkPlanSchedule(param);
    }

    @Override
    @Transactional
    public Map<String, Object> registerMedicalAppointment(Map<String, Object> param) {
        int scheduleId = MapUtil.getInt(param, "scheduleId");

        //检查Redis中是否存在日程缓存（过期的出诊计划和时段会自动删除），不存在缓存就不执行挂号
        String key = "doctor_schedule_" + scheduleId;
        if (!redisTemplate.hasKey(key)) {
            return null;
        }

        //Redis事务代码必须写到execute()回调函数中
        Object execute = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //关注缓存数据（拿到乐观锁的Version）
                operations.watch(key);

                //拿到缓存的数据
                Map entry = operations.opsForHash().entries(key);
                //拿到缓存该时段最大接诊人数和已挂号人数
                int maximum = Integer.parseInt(entry.get("maximum").toString());
                int num = Integer.parseInt(entry.get("num").toString());

                //如果已挂号人数小于最大人数就可以挂号
                if (num < maximum) {
                    //开启Redis事务
                    operations.multi();
                    //已挂号人数+1
                    operations.opsForHash().increment(key, "num", 1);
                    //提交事务
                    return operations.exec();
                }
                //到达挂号人数上限就不执行挂号
                else {
                    operations.unwatch();
                    return null;
                }
            }
        });

        //如果Redis事务提交失败就结束Service方法
        if (execute == null) {
            return null;
        }

        //如果Redis事务提交成功，就执行下面的代码
        try {
            //TODO 创建支付订单
            //TODO 保存挂号记录
            //TODO 更新数据库中的该时段挂号人数和该医生当日挂号实际人数
            //TODO 在Redis中缓存付款记录，并设置过期时间

        } catch (Exception e) {
            if (redisTemplate.hasKey(key)) {
                //恢复缓存该日程已经挂号数量
                redisTemplate.opsForHash().increment(key, "num", -1);
            }
            throw e;
        }
        return param;
    }
}
