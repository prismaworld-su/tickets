package smp.cloud.common.messaging;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

public record TicketSummary(
        UUID ticketId,
        UUID ownerId,
        String ownerName,
        String preview,
        long createdAtEpochMillis
) {

    public TicketSummary {
        if (ticketId == null) throw new IllegalArgumentException("ticketId");
        if (ownerId == null) throw new IllegalArgumentException("ownerId");
        if (ownerName == null) throw new IllegalArgumentException("ownerName");
        if (preview == null) preview = "";
    }

    public void writeTo(DataOutput out) throws IOException {
        out.writeLong(ticketId.getMostSignificantBits());
        out.writeLong(ticketId.getLeastSignificantBits());
        out.writeLong(ownerId.getMostSignificantBits());
        out.writeLong(ownerId.getLeastSignificantBits());
        out.writeUTF(ownerName);
        out.writeUTF(preview);
        out.writeLong(createdAtEpochMillis);
    }

    public static TicketSummary readFrom(DataInput in) throws IOException {
        UUID ticketId = new UUID(in.readLong(), in.readLong());
        UUID ownerId = new UUID(in.readLong(), in.readLong());
        String ownerName = in.readUTF();
        String preview = in.readUTF();
        long createdAt = in.readLong();
        return new TicketSummary(ticketId, ownerId, ownerName, preview, createdAt);
    }
}
