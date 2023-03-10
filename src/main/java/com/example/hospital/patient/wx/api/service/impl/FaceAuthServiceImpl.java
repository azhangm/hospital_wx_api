package com.example.hospital.patient.wx.api.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alipay.service.schema.util.StringUtil;
import com.example.hospital.patient.wx.api.db.dao.FaceAuthDao;
import com.example.hospital.patient.wx.api.db.dao.UserInfoCardDao;
import com.example.hospital.patient.wx.api.db.pojo.FaceAuthEntity;
import com.example.hospital.patient.wx.api.exception.HospitalException;
import com.example.hospital.patient.wx.api.service.FaceAuthService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.iai.v20180301.IaiClient;
import com.tencentcloudapi.iai.v20180301.models.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FaceAuthServiceImpl implements FaceAuthService {

    @Resource
    private FaceAuthDao faceAuthDao;

    @Value("${tencent.cloud.secretId}")
    private String secretId;

    @Value("${tencent.cloud.secretKey}")
    private String secretKey;
    @Value("${tencent.cloud.zmface.groupName}")
    private String groupName;
    @Value("${tencent.cloud.zmface.region}")
    private String region;

    @Resource
    private UserInfoCardDao userInfoCardDao;

    @Override
    public Boolean hasFaceAuthInDay(Map<String,Object> param) {
        Integer id = faceAuthDao.hasFaceAuthInDay(param);
        return id != null;
    }

    /**
     * 录入人脸信息
     *
     * @param map 地图
     */
    @Override
    public void createFaceMode(Map<String, Object> map) {
        Integer userId = (Integer) map.get("userId");
        String photo = (String) map.get("photo");
        // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
        // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议采用更安全的方式来使用密钥，请参见：https://cloud.tencent.com/document/product/1278/85305
        // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
        Credential cred = new Credential(secretId, secretKey);

        // 实例化要请求产品的client对象,clientProfile是可选的
        IaiClient client = new IaiClient(cred,region);
        // 实例化一个请求对象,每个接口都会对应一个request对象

        Map<String, Object> map1 = userInfoCardDao.searchUserInfoCard(userId);
        String name = (String) map1.get("name");
        String sex = (String) map1.get("sex");
        CreatePersonRequest req = new CreatePersonRequest();
        req.setGroupId(groupName);
        req.setPersonId(userId+"");
        long gender = sex.equals("男") ? 1L : 2L;
        req.setPersonName(name);
//        看 api 写代码 写死即可
        req.set("Image",photo);
        req.set("UniquePersonControl",4);
        req.set("QualityControl",4);
        req.set("NeedRotateDetection",1);

//        返回对象 参照案例
        try {
            CreatePersonResponse resp = client.CreatePerson(req);
            String faceId = resp.getFaceId();
            if (StringUtil.isEmpty(faceId)) {
//                录入成功~！！！
                HashMap<String, Object> updateFaceMap = new HashMap<>();
                updateFaceMap.put("userId",userId);
                updateFaceMap.put("existFaceModel",true);
                userInfoCardDao.updateExistFaceModel(updateFaceMap);
            }
        } catch (TencentCloudSDKException e) {
//            全局异常处理。
            throw new HospitalException(e);
        }
    }

    @Override
    @Transactional
    public boolean verifyFaceModel(int userId, String photo) {
        try {
            Credential cred = new Credential(secretId, secretKey);
            IaiClient client = new IaiClient(cred, region);
            //执行人脸识别
            VerifyPersonRequest verifyPersonRequest = new VerifyPersonRequest();
            verifyPersonRequest.setPersonId(userId + "");
            verifyPersonRequest.setImage(photo);
            verifyPersonRequest.setQualityControl(4L);
            VerifyPersonResponse verifyPersonResponse = client.VerifyPerson(verifyPersonRequest);
            boolean face = verifyPersonResponse.getIsMatch();
            if (!face) {
                return false;
            }
            //利用腾讯云执行静态活体检测
            DetectLiveFaceRequest detectLiveFaceRequest = new DetectLiveFaceRequest();
            detectLiveFaceRequest.setImage(photo);
            DetectLiveFaceResponse detectLiveFaceResponse = client.DetectLiveFace(detectLiveFaceRequest);
            boolean living = detectLiveFaceResponse.getIsLiveness();
            //合并人脸识别和活体识别的结果

            //如果身份验证通过，就写入数据库
            if (living) {
                Integer cardId = userInfoCardDao.searchIdByUserId(userId);
                FaceAuthEntity entity = new FaceAuthEntity();
                entity.setPatientCardId(cardId);
                entity.setDate(DateUtil.today());
                faceAuthDao.insert(entity);
            }
            return living;
        }
        catch (TencentCloudSDKException e) {
            log.error("人脸验证失败", e);
            return false;
        }
    }
}
