/*
 *
 * EnergyBuffer.java
 *
 * This file is part of Zero CORE 2 by ZeroNoRyouki, a Minecraft mod.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * DO NOT REMOVE OR EDIT THIS HEADER
 *
 */

package it.zerono.mods.zerocore.lib.energy;

import it.zerono.mods.zerocore.lib.IDebugMessages;
import it.zerono.mods.zerocore.lib.IDebuggable;
import it.zerono.mods.zerocore.lib.data.WideAmount;
import it.zerono.mods.zerocore.lib.data.nbt.ISyncableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.LogicalSide;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class WideEnergyBuffer
        implements IWideEnergyStorage2, ISyncableEntity, IDebuggable {

    public WideEnergyBuffer(EnergySystem system, WideAmount capacity) {
        this(system, capacity, capacity, capacity);
    }

    public WideEnergyBuffer(EnergySystem system, WideAmount capacity, WideAmount maxTransfer) {
        this(system, capacity, maxTransfer, maxTransfer);
    }

    public WideEnergyBuffer(final EnergySystem system, final WideAmount capacity,
                            final WideAmount maxInsert, final WideAmount maxExtract) {

        this._system = system;
        this._energy = WideAmount.ZERO;
        this._capacity = capacity.copy();
        this._maxInsert = maxInsert.copy();
        this._maxExtract = maxExtract.copy();
    }

    public WideEnergyBuffer setCapacity(final WideAmount capacity) {

        this._capacity.set(capacity);

        if (this._energy.greaterThan(capacity)) {
            this._energy.set(capacity);
        }

        return this;
    }

    public WideEnergyBuffer setMaxTransfer(final WideAmount maxTransfer) {

        this.setMaxInsert(maxTransfer);
        this.setMaxExtract(maxTransfer);
        return this;
    }

    public WideEnergyBuffer setMaxInsert(final WideAmount maxInsert) {

        this._maxInsert.set(maxInsert);
        return this;
    }

    public WideEnergyBuffer setMaxExtract(final WideAmount maxExtract) {

        this._maxExtract.set(maxExtract);
        return this;
    }

    public WideAmount getMaxInsert() {
        return this._maxInsert.copy();
    }

    public WideAmount getMaxExtract() {
        return this._maxExtract.copy();
    }

    public WideAmount getEnergyStored() {
        return this._energy.copy();
    }

    public WideEnergyBuffer setEnergyStored(final WideAmount amount) {

        this._energy.set(amount.greaterThan(this._capacity) ? this._capacity : amount);
        return this;
    }

    //region IWideEnergyStorage2

    /**
     * Get the {@code EnergySystem} used natively the the IWideEnergyStorage
     *
     * @return the native {@code EnergySystem}
     */
    public EnergySystem getEnergySystem() {
        return this._system;
    }

    /**
     * Add energy, expressed in the specified {@code EnergySystem}, to the storage
     *
     * @param system the {@link EnergySystem} used by the request
     * @param maxAmount maximum amount of energy to be inserted
     * @param simulate if true, the insertion will only be simulated
     * @return amount of energy that was (or would have been, if simulated) inserted
     */
    @Override
    public WideAmount insertEnergy(final EnergySystem system, WideAmount maxAmount, final boolean simulate) {

        final EnergySystem localSystem = this.getEnergySystem();

        // convert the requested amount to the local energy system
        maxAmount = system.convertTo(localSystem, maxAmount);

        final WideAmount inserted = WideAmount.min(this.getCapacity(localSystem).subtract(this._energy),
                WideAmount.min(this._maxInsert, maxAmount)).copy();

        if (!simulate) {
            this._energy.set(WideAmount.min(this.getEnergyStored(localSystem).add(inserted), this._capacity));
        }

        // convert the inserted energy amount back to the original energy system
        return localSystem.convertTo(system, inserted);
    }

    /**
     * Remove energy, expressed in the specified {@code EnergySystem}, from the storage
     *
     * @param system    the {@link EnergySystem} used by the request
     * @param maxAmount maximum amount of energy to be extracted
     * @param simulate  if true, the extraction will only be simulated
     * @return amount of energy that was (or would have been, if simulated) extracted from the storage
     */
    @Override
    public WideAmount extractEnergy(EnergySystem system, WideAmount maxAmount, boolean simulate) {

        final EnergySystem localSystem = this.getEnergySystem();

        // convert the requested amount to the local energy system
        maxAmount = system.convertTo(localSystem, maxAmount);

        final WideAmount extracted = WideAmount.min(this._energy, WideAmount.min(this._maxExtract, maxAmount)).copy();

        if (!simulate) {
            this._energy.subtract(extracted);
        }

        // convert the extracted energy amount back to the original energy system
        return localSystem.convertTo(system, extracted);
    }

    /**
     * Returns the amount of energy currently stored expressed in the specified {@link EnergySystem}
     *
     * @param system the {@link EnergySystem} used by the request
     */
    @Override
    public WideAmount getEnergyStored(final EnergySystem system) {
        return this.convertIf(system, this._energy);
    }

    /**
     * Returns the maximum amount of energy that can be stored expressed in the specified {@link EnergySystem}
     *
     * @param system the {@link EnergySystem} used by the request
     */
    @Override
    public WideAmount getCapacity(final EnergySystem system) {
        return this.convertIf(system, this._capacity);
    }

    //endregion
    //region ISyncableEntity

    /**
     * Sync the entity data from the given NBT compound
     *
     * @param data the data
     * @param syncReason the reason why the synchronization is necessary
     */
    @Override
    public void syncDataFrom(final CompoundNBT data, final SyncReason syncReason) {

        if (data.contains("wide")) {

            this.syncChildDataEntityFrom(this._capacity, "capacity", data, syncReason);
            this.syncChildDataEntityFrom(this._maxInsert, "maxInsert", data, syncReason);
            this.syncChildDataEntityFrom(this._maxExtract, "maxExtract", data, syncReason);
            this.syncChildDataEntityFrom(this._energy, "energy", data, syncReason);

        } else {

            // load and convert data generated by the old EnergyBuffer class

            this.setMaxInsert(WideAmount.from(data.getDouble("maxInsert")));
            this.setMaxExtract(WideAmount.from(data.getDouble("maxExtract")));
            this.setEnergyStored(WideAmount.from(data.getDouble("energy")));
            this.setCapacity(WideAmount.from(data.getDouble("capacity")));
        }
    }

    /**
     * Sync the entity data to the given NBT compound
     *
     * @param data the data
     * @param syncReason the reason why the synchronization is necessary
     */
    @Override
    public CompoundNBT syncDataTo(final CompoundNBT data, final SyncReason syncReason) {

        data.putByte("wide", (byte)1);
        this.syncChildDataEntityTo(this._capacity, "capacity", data, syncReason);
        this.syncChildDataEntityTo(this._maxInsert, "maxInsert", data, syncReason);
        this.syncChildDataEntityTo(this._maxExtract, "maxExtract", data, syncReason);
        this.syncChildDataEntityTo(this._energy, "energy", data, syncReason);
        return data;
    }

    //endregion
    //region IDebuggable

    @Override
    public void getDebugMessages(final LogicalSide side, final IDebugMessages messages) {

        final EnergySystem sys = this.getEnergySystem();

        messages.add("Energy buffer: %1$s / %2$s; Imax: %3$s/t, Omax: %4$s/t",
                sys.asHumanReadableNumber(this._energy.doubleValue()),
                sys.asHumanReadableNumber(this._capacity.doubleValue()),
                sys.asHumanReadableNumber(this._maxInsert.doubleValue()),
                sys.asHumanReadableNumber(this._maxExtract.doubleValue()));
    }

    //endregion
    //region Object

    @Override
    public String toString() {

        final EnergySystem sys = this.getEnergySystem();

        return String.format("%s / %s - Imax: %s/t, Omax: %s/t",
                sys.asHumanReadableNumber(this._energy.doubleValue()),
                sys.asHumanReadableNumber(this._capacity.doubleValue()),
                sys.asHumanReadableNumber(this._maxInsert.doubleValue()),
                sys.asHumanReadableNumber(this._maxExtract.doubleValue()));
    }

    //endregion
    //region internals

    private WideAmount convertIf(final EnergySystem system, final WideAmount amount) {
        return this.getEnergySystem() != system ? this.getEnergySystem().convertTo(system, amount.copy()) : amount.copy();
    }

    private final EnergySystem _system;
    private final WideAmount _energy;
    private final WideAmount _capacity;
    private final WideAmount _maxInsert;
    private final WideAmount _maxExtract;

    //endregion
}
