package in.wynk.sms.core.repository;

import in.wynk.data.enums.State;
import in.wynk.sms.core.entity.SenderConfigurations;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SenderConfigurationsRepository extends MongoRepository<SenderConfigurations, String> {
    List<SenderConfigurations> findAllByState(State state);
}