package in.wynk.sms.kafka;

public interface IWhatsappSenderHandler<R,T> {
    R send(T t);
}