package in.wynk.sms.service;

public interface IWhatsappMessageTransform<R,T> {
    R transform(T t);
}
