package in.wynk.sms.dto.request;

import com.github.annotation.analytic.core.annotations.AnalysedEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AnalysedEntity
@NoArgsConstructor
@AllArgsConstructor
public class IQDeliveryReportRequest {
    private String messageId;
    private String messageRequestId;
    private String customerId;
    private MetaData metaData;
    private String sourceAddress;
    private String destinationAddress;
    private String message;
    private String messageStatus;
    private String messageType;
    private long requestDate;

    @Getter
    @Builder
    @AnalysedEntity
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaData {
        private String subAccountId;
        private String createdBy;
        private RbacSubAccount rbacSubAccount;
        private String mdrCategory;

        @Getter
        @Builder
        @AnalysedEntity
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RbacSubAccount {
            private String accountId;
            private String firstName;
            private String emailId;
            private String status;
            private String iamUuid;
            private Services services;

            @Getter
            @Builder
            @AnalysedEntity
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Services {
                private SMS SMS;

                @Getter
                @Builder
                @AnalysedEntity
                @NoArgsConstructor
                @AllArgsConstructor
                public static class SMS {
                    private boolean creditFlag;
                    private String serviceStatus;
                    private boolean dltenabled;
                }
            }
        }
    }
}