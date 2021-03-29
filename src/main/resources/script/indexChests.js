"use strict";

sys.log("begin script");

minecraft.goToBlock(215, 66, -122);

const items = minecraft.openContainerAt(217, 66, -122);
let i;

for (i = 0; i < items.size(); i++) {
    let item = items[i];
    if (item.getItem().toString() !== "air") {
        minecraft.quickMoveStack(i);
    }
}

minecraft.closeContainer();

sys.log("finished");
