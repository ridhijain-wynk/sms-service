package in.wynk.sms.constants;

import com.github.annotation.analytic.core.annotations.Analysed;
import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    public static SMSPriority fromString(String priorityStr){
        for(SMSPriority priority: values()){
            if(StringUtils.equalsIgnoreCase(priority.getSmsPriority(), priorityStr)){
                return priority;
            }
        }
        return MEDIUM;
    }

}
