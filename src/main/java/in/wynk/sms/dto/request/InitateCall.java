package in.wynk.sms.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AnalysedEntity
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitateCall {

    @Analysed
    private String callerId;
    @Analysed
    private List<Participant> participants;
    @Analysed
    private TextToSpeech textToSpeech_1;

}
