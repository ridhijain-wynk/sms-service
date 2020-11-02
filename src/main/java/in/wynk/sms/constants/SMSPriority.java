package in.wynk.sms.constants;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.Getter;

@Getter
@AnalysedEntity
public enum SMSPriority {

    HIGH("HIGH",0),
    MEDIUM("MEDIUM",100),
    LOW("LOW",200);


    private final int delay;
    @Analysed
    private final String smsPriority;

    SMSPriority(String priority, int delay) {
        this.smsPriority = priority;
        this.delay = delay;
    }

}
