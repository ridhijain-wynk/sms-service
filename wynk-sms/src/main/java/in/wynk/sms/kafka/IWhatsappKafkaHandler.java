package in.wynk.sms.kafka;

public interface IWhatsappKafkaHandler<T> {
    void sendMessage(T t);
}