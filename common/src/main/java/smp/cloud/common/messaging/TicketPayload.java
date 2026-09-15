package smp.cloud.common.messaging;

import java.io.DataOutput;
import java.io.IOException;

public sealed interface TicketPayload permits OpenGuiPayload, AcceptTicketPayload, StaffStatusPayload {

    byte id();

    void writeTo(DataOutput out) throws IOException;
}
