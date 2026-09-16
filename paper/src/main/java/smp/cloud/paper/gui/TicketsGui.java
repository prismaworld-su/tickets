package smp.cloud.paper.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import smp.cloud.common.messaging.OpenGuiPayload;
import smp.cloud.common.messaging.TicketSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TicketsGui implements InventoryHolder {

    private static final int SIZE = 54;
    private static final int PER_ROW = 7;
    private static final int LEFT_PAD = 1;
    private static final int[] ROW_PRIORITY = {2, 3, 1, 4};

    private final UUID viewerId;
    private final String title;
    private final String hint;
    private final List<TicketSummary> tickets;
    private final Map<Integer, UUID> ticketBySlot = new HashMap<>();
    private Inventory inventory;

    public TicketsGui(OpenGuiPayload payload) {
        this.viewerId = payload.viewerId();
        this.title = payload.title();
        this.hint = payload.hint();
        this.tickets = List.copyOf(payload.tickets());
    }

    public UUID viewerId() {
        return viewerId;
    }

    @Override
    public Inventory getInventory() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(this, SIZE, Component.text(title, NamedTextColor.GOLD));
            populate();
        }
        return inventory;
    }

    public UUID ticketAtSlot(int slot) {
        return ticketBySlot.get(slot);
    }

    private void populate() {
        List<Integer> slots = computeSlots(tickets.size());
        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);
            TicketSummary summary = tickets.get(i);
            inventory.setItem(slot, buildHead(summary));
            ticketBySlot.put(slot, summary.ticketId());
        }
    }

    private ItemStack buildHead(TicketSummary summary) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            PlayerProfile profile = Bukkit.createPlayerProfile(summary.ownerId(), summary.ownerName());
            meta.setOwnerProfile(profile);
            meta.displayName(Component.text(summary.ownerName(), NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(summary.preview(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text(hint, NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    private static List<Integer> computeSlots(int count) {
        if (count == 0) {
            return Collections.emptyList();
        }
        List<Integer> slots = new ArrayList<>(count);
        int placed = 0;
        for (int row : ROW_PRIORITY) {
            if (placed >= count) {
                break;
            }
            int itemsInRow = Math.min(PER_ROW, count - placed);
            int startCol = LEFT_PAD + (PER_ROW - itemsInRow) / 2;
            for (int c = 0; c < itemsInRow; c++) {
                slots.add(row * 9 + startCol + c);
                placed++;
            }
        }
        return slots;
    }
}
