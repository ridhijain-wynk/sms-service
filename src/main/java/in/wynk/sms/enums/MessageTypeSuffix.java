package in.wynk.sms.enums;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AnalysedEntity
@RequiredArgsConstructor
public enum MessageTypeSuffix {

    PROMOTIONAL("-P"), TRANSACTIONAL("-T"), SERVICE_IMPLICIT("-S"), SERVICE_EXPLICIT("-S");

    @Analysed
    private final String suffix;
}
