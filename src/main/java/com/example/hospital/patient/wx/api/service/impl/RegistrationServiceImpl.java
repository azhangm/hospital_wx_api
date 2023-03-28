package com.example.hospital.patient.wx.api.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import com.example.hospital.patient.wx.api.db.dao.*;
import com.example.hospital.patient.wx.api.db.pojo.MedicalRegistrationEntity;
import com.example.hospital.patient.wx.api.service.FaceAuthService;
import com.example.hospital.patient.wx.api.service.PaymentService;
import com.example.hospital.patient.wx.api.service.RegistrationService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@SuppressWarnings("all")
public class RegistrationServiceImpl implements RegistrationService {
    @Resource
    private DoctorWorkPlanDao doctorWorkPlanDao;

    @Resource
    private MedicalRegistrationDao medicalRegistrationDao;

    private final String notifyUrl = "/registration/transactionCallback";

    @Resource
    private UserInfoCardDao userInfoCardDao;

    @Resource
    private FaceAuthService faceAuthService;

    @Resource
    private DoctorWorkPlanScheduleDao doctorWorkPlanScheduleDao;

    @Resource
    private UserDao userDao;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private PaymentService paymentService;

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
        return doctorWorkPlanScheduleDao.searchDoctorWorkPlanSchedule(param);
    }

    @Override
    @Transactional
    public Map<String, Object> registerMedicalAppointment(Map<String, Object> param) {
        int scheduleId = MapUtil.getInt(param, "scheduleId");
        int workPlanId = MapUtil.getInt(param, "workPlanId");

        //检查Redis中是否存在日程缓存（过期的出诊计划和时段会自动删除），不存在缓存就不执行挂号
        String key = "doctor_schedule_" + scheduleId;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
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
            int userId = MapUtil.getInt(param, "userId");
            //查询患者openId字符串，用于创建支付单
            Map<String, Object> map = userDao.searchOpenId(userId);
            String openId = MapUtil.getStr(map, "openId");

            int patientCardId = MapUtil.getInt(map, "patientCardId");
            int doctorId = MapUtil.getInt(param, "doctorId");
            int deptSubId = MapUtil.getInt(param, "deptSubId");
            String date = MapUtil.getStr(param, "date");
            int slot = MapUtil.getInt(param, "slot");
            String temp = MapUtil.getStr(param, "amount");
            int total = NumberUtil.mul(temp, "100").intValue();
            String outTradeNo = IdUtil.simpleUUID().toUpperCase();

            //创建支付订单
            ObjectNode objectNode = paymentService.unifiedOrder(outTradeNo, openId, total, "挂号费", notifyUrl, null);
            //支付单的预支付ID
            String prepayId = objectNode.get("prepay_id").textValue();

            MedicalRegistrationEntity entity = new MedicalRegistrationEntity();
            entity.setWorkPlanId(workPlanId);
            entity.setDoctorScheduleId(scheduleId);
            entity.setPatientCardId(patientCardId);
            entity.setDoctorId(doctorId);
            entity.setDeptSubId(deptSubId);
            entity.setDate(date);
            entity.setSlot(slot);
            entity.setAmount(new BigDecimal(temp));
            entity.setOutTradeNo(outTradeNo);
            entity.setPrepayId(prepayId);
            //保存挂号记录
            medicalRegistrationDao.insert(entity);

            //更新出诊计划实际挂号人数
            doctorWorkPlanDao.updateNumById(new HashMap() {{
                put("id", workPlanId);
                put("n", 1);
            }});
            //更新出诊时段实际挂号人数
            doctorWorkPlanScheduleDao.updateNumById(new HashMap() {{
                put("id", scheduleId);
                put("n", 1);
            }});
            /*
             * 在Redis中缓存付款记录，并设置过期时间。
             * (1) 如果15分钟内患者支付了挂号费，会有Java程序删除该缓存。
             * (2) 如果该缓存过期，说明15分钟内患者没有支付挂号费。
             * 如果患者没有付款，就关闭挂号单，并且恢复缓存和数据库中的已挂号人数。
             */
            redisTemplate.opsForHash().putAll("registration_payment_" + outTradeNo, new HashMap() {{
                put("workPlanId", workPlanId);
                put("scheduleId", scheduleId);
                put("outTradeNo", outTradeNo);
            }});

            DateTime now = new DateTime();
            now.offset(DateField.MINUTE, 15);
            //15分钟后缓存过期
            redisTemplate.expireAt("registration_payment_" + outTradeNo, now);

            HashMap result = new HashMap() {{
                put("outTradeNo", outTradeNo);
                put("prepayId", prepayId);
                put("timeStamp", objectNode.get("timeStamp").asText());
                put("nonceStr", objectNode.get("nonceStr").asText());
                put("package", objectNode.get("package").asText());
                put("signType", objectNode.get("signType").asText());
                put("paySign", objectNode.get("paySign").asText());
            }};
            return result;
        } catch (Exception e) {
            if (redisTemplate.hasKey(key)) {
                //恢复缓存该日程已经挂号数量
                redisTemplate.opsForHash().increment(key, "num", -1);
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public void updatePayment(Map<String,Object> param) {
        String outTradeNo = MapUtil.getStr(param, "outTradeNo");
        //既然患者支付了挂号费，我们就该删除Redis中对应的挂号缓存
        redisTemplate.delete("registration_payment_" + outTradeNo);
        //更新挂号单付款为已付款状态（2状态）
        medicalRegistrationDao.updatePayment(param);
    }
}
