package in.wynk.sms.dto.response;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@AnalysedEntity
public class VoiceSmsResponse {

    @Analysed
    private String status;

    @Analysed
    private String correlationId;
}
