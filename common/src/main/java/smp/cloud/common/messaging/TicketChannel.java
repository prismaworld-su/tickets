package smp.cloud.tickets.common.messaging;

public final class TicketChannel {

    public static final String NAMESPACE = "smp-tickets";
    public static final String NAME = "sync";
    public static final String ID = NAMESPACE + ":" + NAME;

    private TicketChannel() {
    }
}
