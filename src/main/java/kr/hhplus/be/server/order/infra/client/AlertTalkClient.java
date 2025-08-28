package kr.hhplus.be.server.order.infra.client;

public interface AlertTalkClient {
    void sendAlertTalk(Long userId, String message);
}
