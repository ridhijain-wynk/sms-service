package in.wynk.sms.core.service;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.data.dto.IEntityCacheService;
import in.wynk.data.enums.State;
import in.wynk.sms.constants.SmsLoggingMarkers;
import in.wynk.sms.core.entity.SenderConfigurations;
import in.wynk.sms.core.repository.SenderConfigurationsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static in.wynk.common.constant.BaseConstants.IN_MEMORY_CACHE_CRON;
import static in.wynk.data.enums.State.ACTIVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenderConfigurationsCachingService implements IEntityCacheService<SenderConfigurations, String> {

    private final Map<String, SenderConfigurations> SENDER_CONFIG_BY_IDS_CACHE = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final SenderConfigurationsRepository senderConfigRepository;

    @PostConstruct
    @AnalyseTransaction(name = "refreshInMemoryCacheSenderConfigurations")
    @Scheduled(fixedDelay = IN_MEMORY_CACHE_CRON, initialDelay = IN_MEMORY_CACHE_CRON)
    public void init() {
        AnalyticService.update("class", this.getClass().getSimpleName());
        AnalyticService.update("cacheLoadInit", true);
        loadSenderConfigurations();
        AnalyticService.update("cacheLoadCompleted", true);
    }

    public void loadSenderConfigurations() {
        Collection<SenderConfigurations> senderConfigurations = getAllByState(ACTIVE);
        if (CollectionUtils.isNotEmpty(senderConfigurations) && writeLock.tryLock()) {
            Map<String, SenderConfigurations> configIdsMap = new ConcurrentHashMap<>();
            try {
                senderConfigurations.forEach(config -> {
                    configIdsMap.put(config.getId(), config);
                });
                SENDER_CONFIG_BY_IDS_CACHE.clear();
                SENDER_CONFIG_BY_IDS_CACHE.putAll(configIdsMap);
            } catch (Throwable th) {
                log.error(SmsLoggingMarkers.SENDER_CONFIGURATIONS_CACHING_FAILURE, "Exception occurred while refreshing sender configurations cache. Exception: {}", th.getMessage(), th);
                throw th;
            } finally {
                writeLock.unlock();
            }
        }
    }

    public SenderConfigurations getSenderConfigurationsById(String configId) {
        return StringUtils.isNotEmpty(configId) ? SENDER_CONFIG_BY_IDS_CACHE.get(configId) : null;
    }

    public SenderConfigurations getSenderConfigurationsByAliasAndCountry (String alias, String countryCode) {
        return StringUtils.isNotEmpty(alias) ? SENDER_CONFIG_BY_IDS_CACHE.values().stream()
                .filter(config -> config.getClientAlias().equalsIgnoreCase(alias)
                        && config.getCountryCode().equalsIgnoreCase(countryCode)).findAny().orElse(null)
                : null;
    }

    @Override
    public SenderConfigurations get(String key) {
        return SENDER_CONFIG_BY_IDS_CACHE.get(key);
    }

    @Override
    public SenderConfigurations save(SenderConfigurations item) {
        return senderConfigRepository.save(item);
    }

    @Override
    public Collection<SenderConfigurations> getAll() {
        return senderConfigRepository.findAll();
    }

    @Override
    public boolean containsKey(String key) {
        return SENDER_CONFIG_BY_IDS_CACHE.containsKey(key);
    }

    @Override
    public Collection<SenderConfigurations> getAllByState(State state) {
        return senderConfigRepository.findAllByState(state);
    }
}