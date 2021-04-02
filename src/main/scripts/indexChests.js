// @icon minecraft:warped_fungus_on_a_stick
"use strict";

(function() {
    Minecraft.asdasd();
    Minecraft.sendMessage("Hello from Script World");
    if (Minecraft.isAiDisabled()) {
        Minecraft.sendMessage("AI not enabled, exiting");
        return;
    }

    Minecraft.goToBlock(215, 66, -122);

    let stacks = Minecraft.openContainerAt(217, 66, -122);
    for (let i = 0; i < stacks.length; i++) {
        let stack = stacks[i];
        if (stack.getItem() !== "air") {
            Minecraft.quickMoveStack(i);
        }
    }

    Minecraft.closeContainer();

    Minecraft.sendMessage("Script Finished");
})();
