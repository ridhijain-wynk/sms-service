package in.wynk.sms.kinesis;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.wynk.sms.event.ClientPinpointStreamEvent;
import in.wynk.sms.event.PinpointStreamEvent;
import in.wynk.stream.consumer.AbstractKinesisEventConsumer;
import in.wynk.stream.consumer.IKinesisEventHandler;
import in.wynk.stream.consumer.KinesisRecordProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.Executor;

@Slf4j
public class PinpointConsumerService extends AbstractKinesisEventConsumer implements IKinesisEventHandler<PinpointStreamEvent> {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public PinpointConsumerService(String applicationName,
                                   String streamName,
                                   ObjectMapper objectMapper,
                                   KinesisRecordProcessorFactory recordProcessorFactory,
                                   Executor executor) {
        super(applicationName, streamName, objectMapper, recordProcessorFactory, executor);
        recordProcessorFactory.registerHandler(this, objectMapper);
    }

    @Override
    public void consume(PinpointStreamEvent event) {
        eventPublisher.publishEvent(ClientPinpointStreamEvent.builder()
                .clientAlias("enterr10")
                .pinpointEvent(event)
                .build());
    }

    @Override
    public Class<PinpointStreamEvent> eventType() {
        return PinpointStreamEvent.class;
    }

    @Override
    public String getServiceName() {
        return eventType().getName();
    }
}
