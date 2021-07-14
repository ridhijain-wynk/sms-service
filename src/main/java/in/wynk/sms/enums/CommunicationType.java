package in.wynk.sms.enums;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AnalysedEntity
@RequiredArgsConstructor
public enum CommunicationType {
    PROMOTIONAL("PROMOTIONAL"), TRANSACTIONAL("TRANSACTIONAL"), SERVICE_IMPLICIT("SERVICE_IMPLICIT"), SERVICE_EXPLICIT("SERVICE_EXPLICIT"),UNKNOWN("UNKNOWN");

    @Analysed
    private final String type;
}
