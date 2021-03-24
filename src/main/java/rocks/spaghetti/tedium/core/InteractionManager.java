package rocks.spaghetti.tedium.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InteractionManager {
    private static final Queue<InteractionEvent> eventQueue = new ConcurrentLinkedQueue<>();

    public static void pushEvent(InteractionEvent event) {
        eventQueue.add(event);
    }

    public static void tick() {
        if (!eventQueue.isEmpty()) {
            eventQueue.poll().send();
        }
    }

    private interface InteractionEvent {
        void send();
    }

    public static class InventoryPickEvent implements InteractionEvent {
        private final Packet<ServerPlayPacketListener> thePacket;

        public InventoryPickEvent(int slot) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null || player.inventory.selectedSlot == slot) {
                thePacket = null;
            } else {
                if (slot < 9) {
                    player.inventory.selectedSlot = slot;
                    thePacket = new UpdateSelectedSlotC2SPacket(slot);
                } else {
                    thePacket = new PickFromInventoryC2SPacket(slot);
                }
            }
        }

        @Override
        public void send() {
            if (thePacket != null) {
                MinecraftClient.getInstance().getNetworkHandler().sendPacket(thePacket);
            }
        }
    }
}
