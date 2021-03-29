// stubs for ScriptEnvironment

class sys {
    static log(obj);
}

class minecraft {
    static goToBlock(x, y, z);
    /** @return {ItemStack[]} */
    static openContainerAt(x, y, z);
}

class ItemStack {
    /** net.minecraft.items.ItemStack */

    /** @return {boolean} */
    static isEmpty();

    /** @return {Item} */
    static getItem();
}

class Item {

}