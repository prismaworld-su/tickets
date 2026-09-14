package smp.cloud.tickets.common.messaging;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

public record AcceptTicketPayload(UUID ticketId, UUID staffId, String staffName) implements TicketPayload {

    public static final byte ID = 2;

    public AcceptTicketPayload {
        if (ticketId == null) throw new IllegalArgumentException("ticketId");
        if (staffId == null) throw new IllegalArgumentException("staffId");
        if (staffName == null) throw new IllegalArgumentException("staffName");
    }

    @Override
    public byte id() {
        return ID;
    }

    @Override
    public void writeTo(DataOutput out) throws IOException {
        out.writeLong(ticketId.getMostSignificantBits());
        out.writeLong(ticketId.getLeastSignificantBits());
        out.writeLong(staffId.getMostSignificantBits());
        out.writeLong(staffId.getLeastSignificantBits());
        out.writeUTF(staffName);
    }

    public static AcceptTicketPayload readFrom(DataInput in) throws IOException {
        UUID ticketId = new UUID(in.readLong(), in.readLong());
        UUID staffId = new UUID(in.readLong(), in.readLong());
        String staffName = in.readUTF();
        return new AcceptTicketPayload(ticketId, staffId, staffName);
    }
}
