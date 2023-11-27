package in.wynk.sms.controller;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.annotation.analytic.core.annotations.AnalyseTransaction;
import com.github.annotation.analytic.core.service.AnalyticService;
import in.wynk.common.constant.BaseConstants;
import in.wynk.common.dto.WynkResponseEntity;
import in.wynk.data.dto.IEntityCacheService;
import in.wynk.logging.constants.LoggingConstants;
import in.wynk.sms.constants.SmsLoggingMarkers;
import in.wynk.stream.producer.IKafkaEventPublisher;
import in.wynk.wynkservice.core.dao.entity.WynkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/wynk/s2s/iq")
public class NotificationController {

    @Value("${wynk.kafka.producers.whatsapp.iq.inbound.topic}")
    private String whatsappInboundTopic;

    @Value("${wynk.kafka.producers.whatsapp.iq.message.status.topic}")
    private String whatsappMessageStatusTopic;

    private final IEntityCacheService<WynkService, String> serviceCache;

    private final IKafkaEventPublisher<String, Object> kafkaEventPublisher;
    private final ObjectMapper objectMapper;

    private final Map<String, String> migerationServiceMap = new HashMap() {{
        put("airtelxstream", "airteltv");
    }};

    @AnalyseTransaction(name = "inboundNotification")
    @PostMapping(path = "/v1/notifications/inbound/{serviceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public WynkResponseEntity<Void> handleNotification(@PathVariable String serviceId, @RequestBody String payload) {
        final String serviceID = migerationServiceMap.getOrDefault(serviceId, serviceId);
        AnalyticService.update(BaseConstants.SERVICE, serviceID);
        AnalyticService.update(BaseConstants.PAYLOAD, payload);
        final WynkService service = serviceCache.get(serviceID);
        try{
            Object payloadObj = objectMapper.readValue(payload, Object.class);
            final List<Header> headers = new ArrayList<Header>() {{
                add(new RecordHeader(BaseConstants.SERVICE_ID, service.getId().getBytes()));
                add(new RecordHeader(BaseConstants.ORG_ID, service.getLinkedClient().getBytes()));
                add(new RecordHeader(BaseConstants.REQUEST_ID, MDC.get(LoggingConstants.REQUEST_ID).getBytes()));
            }};
            kafkaEventPublisher.publish(whatsappInboundTopic, null, System.currentTimeMillis(), UUIDs.timeBased().toString(), payloadObj, headers);
            AnalyticService.update(payloadObj);
        } catch(Exception e){
            log.error(SmsLoggingMarkers.KAFKA_PUBLISHER_FAILURE, "Unable to publish the inbound notifications in kafka due to {}", e.getMessage(), e);
        }
        return WynkResponseEntity.<Void>builder().build();
    }

    @AnalyseTransaction(name = "messageStatusCallback")
    @PostMapping(path = "/v1/callback/message-status/{serviceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public WynkResponseEntity<Void> handleCallback(@PathVariable String serviceId, @RequestBody String payload) {
        final String serviceID = migerationServiceMap.getOrDefault(serviceId, serviceId);
        AnalyticService.update(BaseConstants.SERVICE, serviceID);
        AnalyticService.update(BaseConstants.PAYLOAD, payload);
        final WynkService service = serviceCache.get(serviceID);
        try{
            Object payloadObj = objectMapper.readValue(payload, Object.class);
            final List<Header> headers = new ArrayList<Header>() {{
                add(new RecordHeader(BaseConstants.SERVICE_ID, service.getId().getBytes()));
                add(new RecordHeader(BaseConstants.ORG_ID, service.getLinkedClient().getBytes()));
                add(new RecordHeader(BaseConstants.REQUEST_ID, MDC.get(LoggingConstants.REQUEST_ID).getBytes()));
            }};
            kafkaEventPublisher.publish(whatsappMessageStatusTopic, null, System.currentTimeMillis(), UUIDs.timeBased().toString(), payloadObj, headers);
            AnalyticService.update(payloadObj);
        } catch(Exception e){
            log.error(SmsLoggingMarkers.KAFKA_PUBLISHER_FAILURE, "Unable to publish the whatsapp message status callback in kafka due to {}", e.getMessage(), e);
        }
        return WynkResponseEntity.<Void>builder().build();
    }
}