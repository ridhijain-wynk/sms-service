package in.wynk.sms.core.service;

import in.wynk.cache.aspect.advice.CachePut;
import in.wynk.cache.aspect.advice.Cacheable;
import in.wynk.sms.dto.request.SmsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static in.wynk.cache.constant.BeanConstant.L2CACHE_MANAGER;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheServiceImpl implements IRedisCacheService {

    @Override
    @CachePut(cacheName = "PinpointSender", cacheKey = "'message-id:'+ #messageId", l2CacheTtl = 60 * 60, cacheManager = L2CACHE_MANAGER)
    public SmsRequest save (String messageId, SmsRequest request){
        log.info("message id - "+messageId+" added in redis. Text - "+request.getText());
        return request;
    }

    @Override
    @Cacheable(cacheName = "PinpointSender", cacheKey = "'message-id:'+ #messageId", l2CacheTtl = 60 * 60, cacheManager = L2CACHE_MANAGER)
    public SmsRequest get (String messageId){
        log.info("message id - "+messageId+" fetched from redis.");
        return null;
    }
}