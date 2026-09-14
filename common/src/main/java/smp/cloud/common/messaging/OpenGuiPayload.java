package smp.cloud.tickets.common.messaging;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record OpenGuiPayload(UUID viewerId, List<TicketSummary> tickets) implements TicketPayload {

    public static final byte ID = 1;

    public OpenGuiPayload {
        if (viewerId == null) throw new IllegalArgumentException("viewerId");
        tickets = List.copyOf(tickets);
    }

    @Override
    public byte id() {
        return ID;
    }

    @Override
    public void writeTo(DataOutput out) throws IOException {
        out.writeLong(viewerId.getMostSignificantBits());
        out.writeLong(viewerId.getLeastSignificantBits());
        out.writeInt(tickets.size());
        for (TicketSummary summary : tickets) {
            summary.writeTo(out);
        }
    }

    public static OpenGuiPayload readFrom(DataInput in) throws IOException {
        UUID viewerId = new UUID(in.readLong(), in.readLong());
        int size = in.readInt();
        if (size < 0) {
            throw new IOException("Negative ticket count: " + size);
        }
        List<TicketSummary> tickets = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            tickets.add(TicketSummary.readFrom(in));
        }
        return new OpenGuiPayload(viewerId, tickets);
    }
}
