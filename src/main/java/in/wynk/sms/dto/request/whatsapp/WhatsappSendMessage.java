package in.wynk.sms.dto.request.whatsapp;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.stream.advice.KafkaEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@SuperBuilder
@AnalysedEntity
@RequiredArgsConstructor
public class WhatsappSendMessage implements Serializable {
    @NotNull
    @Analysed
    private String messageType;
    @Analysed
    private WhatsappMessageRequest messageRequest;
}