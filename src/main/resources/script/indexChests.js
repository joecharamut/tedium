"use strict";

(function() {
    minecraft.sendMessage("Hello from Script World");
    if (!minecraft.aiEnabled()) {
        minecraft.sendMessage("AI not enabled, exiting");
        return;
    }

    minecraft.goToBlock(215, 66, -122);

    let stacks = minecraft.openContainerAt(217, 66, -122);
    for (let i = 0; i < stacks.length; i++) {
        let stack = stacks[i];
        if (stack.getItem() !== "air") {
            minecraft.quickMoveStack(i);
        }
    }

    minecraft.closeContainer();

    minecraft.sendMessage("Script Finished");
})();
