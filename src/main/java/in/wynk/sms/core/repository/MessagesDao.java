package in.wynk.sms.core.repository;

import in.wynk.sms.core.entity.Messages;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessagesDao extends MongoRepository<Messages, String> {
}
