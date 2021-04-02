// stubs for ScriptEnvironment

class Sys {
    /** @param {any} obj */
    static log(obj);
}

class Minecraft {
    /** @param {string} message */
    static sendMessage(message);

    /** @return {boolean} */
    static isAiDisabled();

    /**
     * @param {number} x
     * @param {number} y
     * @param {number} z
     */
    static goToBlock(x, y, z);

    /**
     * @param {number} x
     * @param {number} y
     * @param {number} z
     * @return {ItemStack[]}
     */
    static openContainerAt(x, y, z);

    static closeContainer();

    /**
     * @param {number} slot
     * @return {boolean}
     */
    static quickMoveStack(slot);

    /**
     * @param {number} x
     * @param {number} y
     * @param {number} z
     * @return {BlockState}
     */
    static getBlockStateAt(x, y, z);
}

class ItemStack {
    /** net.minecraft.items.ItemStack */

    /** @return {boolean} */
    static isEmpty();

    /** @return {Item} */
    static getItem();
}

class Item {
    /** net.minecraft.items.Item */

    /** @return {string} */
    static toString();
}

class BlockState {

}

