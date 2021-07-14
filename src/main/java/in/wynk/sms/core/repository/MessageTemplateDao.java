package in.wynk.sms.core.repository;

import in.wynk.data.enums.State;
import in.wynk.sms.core.entity.MessageTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageTemplateDao extends MongoRepository<MessageTemplate, String> {
    List<MessageTemplate> getMessageTemplateByState(State state);
}
