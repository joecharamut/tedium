// stubs for ScriptEnvironment

const Sys: SysClass = {};
class SysClass {
    /** @param {any} obj */
    log: function;
}

const Util: UtilClass = {};
class UtilClass {
    /**
     * @param {number} x
     * @param {number} y
     * @param {number} z
     * @return {BlockPos}
     */
    blockPosOf: function;

    /**
     * @param {number} x
     * @param {number} y
     * @param {number} z
     * @return {Vec3d}
     */
    vec3dOf: function;
}

const Minecraft: MinecraftClass = {};
class MinecraftClass {
    /** @param {string} message */
    sendMessage: function;

    /** @return {boolean} */
    isAiDisabled: function;

    /**
     * @param {number} x
     * @param {number} y
     * @param {number} z
     */
    goToBlock: function;

    /**
     * @param {number} x
     * @param {number} y
     * @param {number} z
     * @return {ItemStack[]}
     */
    openContainerAt: function;

    closeContainer: function;

    /**
     * @param {number} slot
     * @return {boolean}
     */
    quickMoveStack: function;

    /**
     * @param {number} x
     * @param {number} y
     * @param {number} z
     * @return {BlockState}
     */
    getBlockStateAt: function;

    /** @return {BlockPos} player position */
    getPos: function;

    /** @param {string} direction */
    faceDirection: function;

    /** @param {number} amount */
    moveForward: function;
}

class ItemStack {
    /** net.minecraft.items.ItemStack */

    /** @return {boolean} */
    isEmpty: function;

    /** @return {Item} */
    getItem: function;
}

class Item {
    /** net.minecraft.items.Item */

    /** @return {string} */
    static toString();
}

class BlockState {

}

class BlockPos {
    /** net.minecraft.util.math */

    /** @return {number} */
    getX: function;

    /** @return {number} */
    getY: function;

    /** @return {number} */
    getZ: function;
}

