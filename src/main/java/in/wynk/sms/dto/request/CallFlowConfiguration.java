package in.wynk.sms.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AnalysedEntity
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallFlowConfiguration {

    @Analysed
    private InitateCall initateCall_1;
    @Analysed
    private TextToSpeech textToSpeech_1;

}
