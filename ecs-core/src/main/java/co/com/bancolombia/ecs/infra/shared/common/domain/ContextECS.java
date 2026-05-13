package co.com.bancolombia.ecs.infra.shared.common.domain;

public final class ContextECS {

    /*
    * Deberia retornar un nulo si el parametro esta en false o no esta activo
     */
    public static final String KEY_MESSAGE_ID = "ECS_MESSAGE_ID";

    private static final ThreadLocal<String> messageIdHolder = new ThreadLocal<>();

    private ContextECS() {}

    public static void setMessageId(String messageId) {
        messageIdHolder.set(messageId);
    }

    public static String getMessageId() {
        return messageIdHolder.get();
    }

    public static void clear() {
        messageIdHolder.remove();
    }
}