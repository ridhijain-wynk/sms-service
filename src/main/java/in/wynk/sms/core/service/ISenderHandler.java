package in.wynk.sms.core.service;

import in.wynk.sms.dto.MessageDetails;

public interface ISenderHandler<T extends MessageDetails> {
    void handle(T messageStrategy) throws Exception;
}