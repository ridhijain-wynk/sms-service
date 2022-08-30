package in.wynk.sms.core.repository;

import in.wynk.data.enums.State;
import in.wynk.sms.core.entity.Messages;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessagesRepository extends MongoRepository<Messages, String> {
    List<Messages> getMessagesByState(State state);
}
