package in.wynk.sms.core.entity;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VoiceProperties {
    private String callType;
    private String textType;
    private Integer maxRetry;
    private String customerId;
    private String token;
    private String callerId;
    private String callFlowId;
}