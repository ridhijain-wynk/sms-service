package in.wynk.sms.sender;

public interface IMessageSender<T> {

    void sendMessage(T request) throws Exception;

}
