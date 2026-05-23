package com.travel.travelweb.service;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponseBody;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.travel.travelweb.config.AliyunSmsProperties;
import darabonba.core.client.ClientOverrideConfiguration;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AliyunSmsService {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsService.class);

    private final AliyunSmsProperties properties;
    private volatile AsyncClient client;

    public AliyunSmsService(AliyunSmsProperties properties) {
        this.properties = properties;
    }

  /** 发送短信验证码，返回用于核验的 outId */
    public String sendVerifyCode(String phoneNumber) {
        ensureConfigured();
        String outId = UUID.randomUUID().toString().replace("-", "");

        SendSmsVerifyCodeRequest.Builder builder = SendSmsVerifyCodeRequest.builder()
                .phoneNumber(phoneNumber)
                .signName(properties.getSignName())
                .templateCode(properties.getTemplateCode())
                .countryCode(properties.getCountryCode())
                .outId(outId)
                .codeLength((long) properties.getCodeLength())
                .validTime((long) properties.getValidTime())
                .interval((long) properties.getInterval())
                .returnVerifyCode(false)
                .templateParam("{\"code\":\"##code##\",\"min\":\"" + (properties.getValidTime() / 60) + "\"}");

        if (properties.getSchemeName() != null && !properties.getSchemeName().isBlank()) {
            builder.schemeName(properties.getSchemeName());
        }

        try {
            SendSmsVerifyCodeResponse response = getClient()
                    .sendSmsVerifyCode(builder.build())
                    .get(15, TimeUnit.SECONDS);
            SendSmsVerifyCodeResponseBody body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.getSuccess()) || !"OK".equals(body.getCode())) {
                String message = body != null ? body.getMessage() : "未知错误";
                log.error("短信发送失败: phone={}, code={}, message={}", phoneNumber,
                        body != null ? body.getCode() : null, message);
                if (message != null && (message.contains("签名") || message.contains("模板"))) {
                    throw new IllegalStateException(
                            "短信发送失败：" + message + "。请使用号码认证控制台「赠送模板配置」中的签名和模板 CODE，"
                                    + "不能使用短信服务(Dysmsapi)里 SMS_ 开头的自定义模板。");
                }
                if ((body != null && "isv.OUT_OF_SERVICE".equals(body.getCode()))
                        || (message != null && message.toLowerCase().contains("insufficient balance"))) {
                    throw new IllegalStateException("短信发送失败：阿里云账户余额不足，请登录阿里云控制台充值后再试。");
                }
                throw new IllegalStateException("短信发送失败：" + message);
            }
            SendSmsVerifyCodeResponseBody.Model model = body.getModel();
            String returnedOutId = model != null && model.getOutId() != null ? model.getOutId() : outId;
            log.info("短信验证码已发送: phone={}", phoneNumber);
            return returnedOutId;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("短信发送异常: phone={}", phoneNumber, e);
            throw new IllegalStateException("短信发送失败，请稍后重试");
        }
    }

    /** 调用阿里云接口核验验证码 */
    public boolean checkVerifyCode(String phoneNumber, String verifyCode, String outId) {
        ensureConfigured();
        if (outId == null || outId.isBlank()) {
            return false;
        }

        CheckSmsVerifyCodeRequest.Builder builder = CheckSmsVerifyCodeRequest.builder()
                .phoneNumber(phoneNumber)
                .verifyCode(verifyCode)
                .outId(outId)
                .countryCode(properties.getCountryCode());

        if (properties.getSchemeName() != null && !properties.getSchemeName().isBlank()) {
            builder.schemeName(properties.getSchemeName());
        }

        try {
            CheckSmsVerifyCodeResponse response = getClient()
                    .checkSmsVerifyCode(builder.build())
                    .get(15, TimeUnit.SECONDS);
            CheckSmsVerifyCodeResponseBody body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.getSuccess()) || !"OK".equals(body.getCode())) {
                log.warn("验证码核验请求失败: phone={}, message={}", phoneNumber,
                        body != null ? body.getMessage() : null);
                return false;
            }
            CheckSmsVerifyCodeResponseBody.Model model = body.getModel();
            return model != null && "PASS".equals(model.getVerifyResult());
        } catch (Exception e) {
            log.error("验证码核验异常: phone={}", phoneNumber, e);
            return false;
        }
    }

    private void ensureConfigured() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("短信服务未启用，请在 application.yml 中配置 aliyun.sms");
        }
        if (isBlank(properties.getAccessKeyId()) || isBlank(properties.getAccessKeySecret())
                || properties.getAccessKeyId().startsWith("your-")
                || properties.getAccessKeySecret().startsWith("your-")) {
            throw new IllegalStateException("请配置阿里云 AccessKey（环境变量 ALIYUN_ACCESS_KEY_ID / ALIYUN_ACCESS_KEY_SECRET）");
        }
        if (isBlank(properties.getSignName()) || isBlank(properties.getTemplateCode())) {
            throw new IllegalStateException("请配置短信签名 sign-name 与模板 template-code");
        }
    }

    private AsyncClient getClient() {
        AsyncClient current = client;
        if (current == null) {
            synchronized (this) {
                current = client;
                if (current == null) {
                    StaticCredentialProvider provider = StaticCredentialProvider.create(
                            Credential.builder()
                                    .accessKeyId(properties.getAccessKeyId())
                                    .accessKeySecret(properties.getAccessKeySecret())
                                    .build());
                    client = AsyncClient.builder()
                            .region("cn-hangzhou")
                            .credentialsProvider(provider)
                            .overrideConfiguration(
                                    ClientOverrideConfiguration.create()
                                            .setEndpointOverride("dypnsapi.aliyuncs.com"))
                            .build();
                    current = client;
                }
            }
        }
        return current;
    }

    @PreDestroy
    public void close() {
        if (client != null) {
            client.close();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
