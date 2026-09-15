package smp.cloud.common.messaging;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

public record StaffStatusPayload(UUID playerId, boolean staff) implements TicketPayload {

    public static final byte ID = 3;

    public StaffStatusPayload {
        if (playerId == null) {
            throw new IllegalArgumentException("playerId");
        }
    }

    @Override
    public byte id() {
        return ID;
    }

    @Override
    public void writeTo(DataOutput out) throws IOException {
        out.writeLong(playerId.getMostSignificantBits());
        out.writeLong(playerId.getLeastSignificantBits());
        out.writeBoolean(staff);
    }

    public static StaffStatusPayload readFrom(DataInput in) throws IOException {
        UUID playerId = new UUID(in.readLong(), in.readLong());
        boolean staff = in.readBoolean();
        return new StaffStatusPayload(playerId, staff);
    }
}
