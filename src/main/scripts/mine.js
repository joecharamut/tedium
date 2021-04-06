// @icon minecraft:warped_fungus_on_a_stick
"use strict";

(function() {
    Minecraft.sendMessage("Hello from Script World");

    if (Minecraft.isAiDisabled()) {
        Minecraft.sendMessage("AI not enabled, exiting");
        return;
    }

    let playerPos = Minecraft.getPos();
    if (playerPos.getY() !== 11) {
        Minecraft.sendMessage("y=11 needed for strip mining");
        return;
    }

    Minecraft.faceDirection("WEST");
    Minecraft.moveForward(5);

    Minecraft.sendMessage("Script Finished");
})();
