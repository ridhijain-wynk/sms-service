package in.wynk.sms.core.service;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.data.dto.IEntityCacheService;
import in.wynk.data.enums.State;
import in.wynk.sms.constants.SmsLoggingMarkers;
import in.wynk.sms.core.entity.Senders;
import in.wynk.sms.core.repository.SendersRepository;
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
public class SendersCachingService implements IEntityCacheService<Senders, String> {

    private final Map<String, Senders> SENDER_BY_IDS_CACHE = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final SendersRepository sendersRepository;

    @PostConstruct
    @AnalyseTransaction(name = "refreshInMemoryCacheSenders")
    @Scheduled(fixedDelay = IN_MEMORY_CACHE_CRON, initialDelay = IN_MEMORY_CACHE_CRON)
    public void init() {
        AnalyticService.update("class", this.getClass().getSimpleName());
        AnalyticService.update("cacheLoadInit", true);
        loadSenders();
        AnalyticService.update("cacheLoadCompleted", true);
    }

    public void loadSenders() {
        Collection<Senders> senders = getAllByState(ACTIVE);
        if (CollectionUtils.isNotEmpty(senders) && writeLock.tryLock()) {
            Map<String, Senders> senderIdsMap = new ConcurrentHashMap<>();
            try {
                senders.forEach(sender -> senderIdsMap.put(sender.getId(), sender));
                SENDER_BY_IDS_CACHE.clear();
                SENDER_BY_IDS_CACHE.putAll(senderIdsMap);
            } catch (Throwable th) {
                log.error(SmsLoggingMarkers.SENDERS_CACHING_FAILURE, "Exception occurred while refreshing senders cache. Exception: {}", th.getMessage(), th);
                throw th;
            } finally {
                writeLock.unlock();
            }
        }
    }

    public Senders getSenderById(String id) {
        return StringUtils.isNotEmpty(id) ? SENDER_BY_IDS_CACHE.get(id) : null;
    }

    public Senders getSenderByNameAndClient(String name, String clientAlias) {
        return SENDER_BY_IDS_CACHE.values().stream().filter(senders -> senders.getClientAlias().equalsIgnoreCase(clientAlias) && senders.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    @Override
    public Senders get(String key) {
        return SENDER_BY_IDS_CACHE.get(key);
    }

    @Override
    public Senders save(Senders item) {
        return sendersRepository.save(item);
    }

    @Override
    public Collection<Senders> getAll() {
        return sendersRepository.findAll();
    }

    @Override
    public boolean containsKey(String key) {
        return SENDER_BY_IDS_CACHE.containsKey(key);
    }

    @Override
    public Collection<Senders> getAllByState(State state) {
        return sendersRepository.findAllByState(state);
    }
}