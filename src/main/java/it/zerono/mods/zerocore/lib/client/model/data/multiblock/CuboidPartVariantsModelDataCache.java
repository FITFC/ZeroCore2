/*
 *
 * CuboidPartVariantsModelDataCache.java
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

package it.zerono.mods.zerocore.lib.client.model.data.multiblock;

import com.google.common.collect.Maps;
import it.zerono.mods.zerocore.lib.block.BlockFacings;
import net.minecraftforge.common.util.NonNullSupplier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CuboidPartVariantsModelDataCache {

    public CuboidPartVariantsModelDataCache() {
        _cache = Maps.newHashMap(new ConcurrentHashMap<>());
    }

    public CuboidPartVariantsModelData computeIfAbsent(final int blockId, final int variantIndex, final BlockFacings outwardFacing,
                                                       final NonNullSupplier<CuboidPartVariantsModelData> missingDataSupplier) {
        return this._cache.computeIfAbsent(CuboidPartVariantsModelData.hash(blockId, variantIndex, outwardFacing),
                k -> missingDataSupplier.get());
    }

    //region internals

    private final Map<Integer, CuboidPartVariantsModelData> _cache;

    //endregion
}
