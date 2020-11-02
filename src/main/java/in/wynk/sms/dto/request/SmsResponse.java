package in.wynk.sms.dto.request;

import in.wynk.commons.dto.BaseResponse;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SmsResponse extends BaseResponse<Void> {


    @Override
    public Void getData() {
        return null;
    }
}
