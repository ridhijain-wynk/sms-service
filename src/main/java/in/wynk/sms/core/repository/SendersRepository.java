package in.wynk.sms.core.repository;

import in.wynk.data.enums.State;
import in.wynk.sms.core.entity.Senders;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SendersRepository extends MongoRepository<Senders, String> {
    List<Senders> findAllByState(State state);
}