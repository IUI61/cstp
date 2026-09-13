package org.example.cstp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cstp.service.GoodsCacheService;
import org.example.cstp.vo.HomeGoodsVo;
import org.example.cstp.vo.PageResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

@Service
public class GoodsCacheServiceImpl implements GoodsCacheService {
    private static final Logger log = LoggerFactory.getLogger(GoodsCacheServiceImpl.class);
    /** 列表缓存版本号：写操作自增它 = 一次性失效所有页 */
    private static final String VERSION_KEY = "goods:list:ver";
    private static final String PAGE_KEY_PREFIX = "goods:page:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public GoodsCacheServiceImpl(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /** 读取当前版本号（未初始化时按 0 处理，第一次自增后变 1） */
    @Override
    public long version() {
        try {
            String value = stringRedisTemplate.opsForValue().get(VERSION_KEY);
            return value == null ? 0L : Long.parseLong(value);
        } catch (Exception e) {
            log.warn("读取商品列表缓存版本号失败，按 0 处理", e);
            return 0L;
        }
    }

    /** 未命中或缓存异常都返回 null，调用方降级查库（缓存永远不能拖垮主流程） */
    @Override
    public PageResultVo<HomeGoodsVo> getPage(long version, int pageNum) {
        String key = pageKey(version, pageNum);
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, new TypeReference<PageResultVo<HomeGoodsVo>>() {});
        } catch (JsonProcessingException e) {
            // 结构对不上（比如以后 VO 改了字段）：删掉坏缓存降级查库，而不是让接口报 500
            log.warn("商品分页缓存反序列化失败，已删除, key={}", key);
            safeDelete(key);
            return null;
        } catch (Exception e) {
            log.warn("访问 Redis 失败，降级查库, key={}", key);
            return null;
        }
    }

    /**
     * 回填时使用"读库那一刻"的版本号快照：
     * 若期间有人发布商品（版本号已推进），这条数据会写到旧版本的 key 上，新 key 依然干净
     **/
    @Override
    public void putPage(long version, int pageNum, PageResultVo<HomeGoodsVo> pageResult) {
        String key = pageKey(version, pageNum);
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(pageResult), TTL);
        } catch (Exception e) {
            log.warn("写入商品分页缓存失败，忽略, key={}", key, e);
        }
    }

    /**
     * 所有会改变"商品列表"的写操作（发布 / 上下架 / 以后售出）都调这里。
     * 只推进版本号，不逐个删 key：旧 key 无人再读，被 TTL 自然淘汰。
     */
    @Override
    public void invalidateList() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 事务提交成功后再失效：防止"事务未提交时缓存已被并发请求用旧数据回填"；回滚则不用失效，因为库没变
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    bumpVersion();
                }
            });
        } else {
            bumpVersion();
        }
    }

    private void bumpVersion() {
        try {
            stringRedisTemplate.opsForValue().increment(VERSION_KEY);
        } catch (Exception e) {
            log.warn("商品列表缓存版本号自增失败", e);
        }
    }

    private String pageKey(long version, int pageNum) {
        return PAGE_KEY_PREFIX + version + ":" + pageNum;
    }

    private void safeDelete(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("删除缓存失败, key={}", key, e);
        }
    }
}
