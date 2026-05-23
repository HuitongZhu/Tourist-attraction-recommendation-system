package com.travel.travelweb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * 短信验证码会话 Redis 缓存：替代内存 ConcurrentHashMap，
 * 支持服务重启后验证码状态保留、多实例共享。
 */
@Service
public class SmsCodeCacheService {

    private static final Logger log = LoggerFactory.getLogger(SmsCodeCacheService.class);

    private static final String KEY_PREFIX = "travel:sms:";
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration SEND_INTERVAL = Duration.ofSeconds(60);
    private static final char SEPARATOR = '|';

    private final StringRedisTemplate redisTemplate;

    public SmsCodeCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 发送前检查 60 秒频率限制 */
    public void checkSendInterval(String phone, SmsCodeType type) {
        SmsSession session = getSession(phone, type);
        if (session != null && session.sendTime.plus(SEND_INTERVAL).isAfter(Instant.now())) {
            throw new IllegalArgumentException("验证码发送过于频繁，请稍后再试");
        }
    }

    /** 保存验证码会话（outId），TTL 5 分钟 */
    public void saveSession(String phone, SmsCodeType type, String outId) {
        String key = buildKey(phone, type);
        String value = outId + SEPARATOR + Instant.now().toEpochMilli();
        redisTemplate.opsForValue().set(key, value, CODE_TTL);
        log.info("短信会话已写入 Redis: key={}", key);
    }

    /** 获取 outId；会话不存在或已过期返回 null */
    public String getOutId(String phone, SmsCodeType type) {
        SmsSession session = getSession(phone, type);
        return session != null ? session.outId : null;
    }

    /** 验证成功后删除会话，防止重复使用 */
    public void removeSession(String phone, SmsCodeType type) {
        redisTemplate.delete(buildKey(phone, type));
    }

    private SmsSession getSession(String phone, SmsCodeType type) {
        String raw = redisTemplate.opsForValue().get(buildKey(phone, type));
        if (raw == null || raw.isBlank()) {
            return null;
        }
        int sep = raw.indexOf(SEPARATOR);
        if (sep <= 0 || sep >= raw.length() - 1) {
            log.warn("Redis 短信会话格式异常: type={}, phone={}", type, maskPhone(phone));
            return null;
        }
        String outId = raw.substring(0, sep);
        try {
            long millis = Long.parseLong(raw.substring(sep + 1));
            return new SmsSession(outId, Instant.ofEpochMilli(millis));
        } catch (NumberFormatException e) {
            log.warn("Redis 短信会话时间戳解析失败: type={}, phone={}", type, maskPhone(phone));
            return null;
        }
    }

    private static String buildKey(String phone, SmsCodeType type) {
        return KEY_PREFIX + type.keySegment() + ":" + phone.trim();
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private record SmsSession(String outId, Instant sendTime) {}
}
