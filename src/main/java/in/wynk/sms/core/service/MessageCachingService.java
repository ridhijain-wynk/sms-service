package in.wynk.sms.core.service;

import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.sms.core.entity.Messages;
import in.wynk.sms.core.repository.MessagesDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static in.wynk.common.constant.BaseConstants.IN_MEMORY_CACHE_CRON;
import static in.wynk.logging.BaseLoggingMarkers.APPLICATION_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCachingService {

    private final Map<String, Messages> messagesMap = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final MessagesDao messagesDao;

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
        Collection<Messages> allMessages = messagesDao.findAll();
        if (CollectionUtils.isNotEmpty(allMessages) && writeLock.tryLock()) {
            try {
                Map<String, Messages> temp = allMessages.stream().collect(Collectors.toMap(Messages::getId, Function.identity()));
                messagesMap.clear();
                messagesMap.putAll(temp);
            } catch (Throwable th) {
                log.error(APPLICATION_ERROR, "Exception occurred while refreshing messages cache. Exception: {}", th.getMessage(), th);
                throw th;
            } finally {
                writeLock.unlock();
            }
        }
    }

    public Messages get(String key) {
        return messagesMap.get(key);
    }

    public boolean containsKey(String key) {
        return messagesMap.containsKey(key);
    }

}