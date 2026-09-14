package smp.cloud.common.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class TicketProtocol {

    private TicketProtocol() {
    }

    public static byte[] encode(TicketPayload payload) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(buffer)) {
            out.writeByte(payload.id());
            payload.writeTo(out);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to encode ticket payload", e);
        }
        return buffer.toByteArray();
    }

    public static TicketPayload decode(byte[] data) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            byte id = in.readByte();
            return switch (id) {
                case OpenGuiPayload.ID -> OpenGuiPayload.readFrom(in);
                case AcceptTicketPayload.ID -> AcceptTicketPayload.readFrom(in);
                default -> throw new IOException("Unknown ticket payload id: " + id);
            };
        }
    }
}
