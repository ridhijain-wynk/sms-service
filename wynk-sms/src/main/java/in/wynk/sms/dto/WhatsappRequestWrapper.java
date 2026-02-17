package in.wynk.sms.dto;

import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import in.wynk.sms.dto.request.WhatsappRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AnalysedEntity
@RequiredArgsConstructor
public class WhatsappRequestWrapper {
    private String clientAlias;
    private String WABANumber;
    private WhatsappRequest request;
}
