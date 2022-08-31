package in.wynk.sms.core.service;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.data.dto.IEntityCacheService;
import in.wynk.data.enums.State;
import in.wynk.sms.core.entity.Messages;
import in.wynk.sms.core.repository.MessagesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static in.wynk.common.constant.BaseConstants.IN_MEMORY_CACHE_CRON;
import static in.wynk.common.constant.CacheBeanNameConstants.MESSAGES;
import static in.wynk.logging.BaseLoggingMarkers.APPLICATION_ERROR;

@Slf4j
@Service(value = MESSAGES)
@RequiredArgsConstructor
public class MessageCachingService implements IEntityCacheService<Messages, String> {

    private final Map<String, Messages> MESSAGES_BY_IDS_CACHE = new ConcurrentHashMap<>();
    private final Map<String, Messages> MESSAGES_BY_TEMPLATE_IDS_CACHE = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final MessagesRepository messagesRepository;

    @PostConstruct
    @AnalyseTransaction(name = "refreshInMemoryCacheMessages")
    @Scheduled(fixedDelay = IN_MEMORY_CACHE_CRON, initialDelay = IN_MEMORY_CACHE_CRON)
    public void init() {
        AnalyticService.update("class", this.getClass().getSimpleName());
        AnalyticService.update("cacheLoadInit", true);
        loadMessages();
        AnalyticService.update("cacheLoadCompleted", true);
    }

    private void loadMessages() {
        Collection<Messages> allMessages = messagesRepository.getMessagesByState(State.ACTIVE);
        if (CollectionUtils.isNotEmpty(allMessages) && writeLock.tryLock()) {
            try {
                Map<String, Messages> idMap = allMessages.stream().collect(Collectors.toMap(Messages::getId, Function.identity()));
                Map<String, Messages> templateIdMap = allMessages.stream().filter(m -> Objects.nonNull(m.getTemplateId())).collect(Collectors.toMap(Messages::getTemplateId, Function.identity()));
                MESSAGES_BY_IDS_CACHE.clear();
                MESSAGES_BY_TEMPLATE_IDS_CACHE.clear();
                MESSAGES_BY_IDS_CACHE.putAll(idMap);
                MESSAGES_BY_TEMPLATE_IDS_CACHE.putAll(templateIdMap);
            } catch (Throwable th) {
                log.error(APPLICATION_ERROR, "Exception occurred while refreshing messages cache. Exception: {}", th.getMessage(), th);
                throw th;
            } finally {
                writeLock.unlock();
            }
        }
    }

    @Override
    public Messages get(String key) {
        return MESSAGES_BY_IDS_CACHE.getOrDefault(key, null);
    }

    public Messages getMessageByTemplateId(String key) {
        return MESSAGES_BY_TEMPLATE_IDS_CACHE.getOrDefault(key, null);
    }

    @Override
    public Messages save(Messages item) {
        return messagesRepository.save(item);
    }

    @Override
    public Collection<Messages> getAll() {
        return MESSAGES_BY_IDS_CACHE.values();
    }

    @Override
    public boolean containsKey(String key) {
        return MESSAGES_BY_IDS_CACHE.containsKey(key);
    }

    @Override
    public Collection<Messages> getAllByState(State state) {
        return messagesRepository.getMessagesByState(state);
    }
}