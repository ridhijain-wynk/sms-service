package in.wynk.sms.event;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AnalysedEntity
@NoArgsConstructor
@AllArgsConstructor
public class ClientPinpointStreamEvent {
    @Analysed
    private String clientAlias;
    @Analysed
    private PinpointStreamEvent pinpointEvent;
}
