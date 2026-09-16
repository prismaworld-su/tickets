package smp.cloud.velocity.config;

public record TicketingConfig(boolean enabled, String staffPermission) {

    public TicketingConfig {
        if (staffPermission == null || staffPermission.isBlank()) {
            throw new IllegalArgumentException("staffPermission must not be blank");
        }
    }

    public static TicketingConfig defaults() {
        return new TicketingConfig(true, "tickets.staff");
    }
}
