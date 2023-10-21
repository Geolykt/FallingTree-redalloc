package fr.raksrinana.fallingtree.fabric.event;

import fr.raksrinana.fallingtree.common.FallingTreeCommon;
import fr.raksrinana.fallingtree.common.config.enums.BreakMode;
import fr.raksrinana.fallingtree.common.tree.BreakTreeResult;
import fr.raksrinana.fallingtree.common.tree.exception.NoTreeFoundException;
import fr.raksrinana.fallingtree.common.tree.exception.NotServerException;
import fr.raksrinana.fallingtree.common.tree.exception.PlayerNotInRightState;
import fr.raksrinana.fallingtree.common.tree.exception.ToolUseForcedException;
import fr.raksrinana.fallingtree.common.tree.exception.TreeBreakingException;
import fr.raksrinana.fallingtree.common.tree.exception.TreeBreakingNotEnabledException;
import fr.raksrinana.fallingtree.fabric.common.wrapper.BlockPosWrapper;
import fr.raksrinana.fallingtree.fabric.common.wrapper.LevelWrapper;
import fr.raksrinana.fallingtree.fabric.common.wrapper.PlayerWrapper;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class BlockBreakListener implements PlayerBlockBreakEvents.Before{
	@NotNull
	private final FallingTreeCommon<?> mod;
	
	@Override
	public boolean beforeBlockBreak(Level level, Player player, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity){
		var wrappedPlayer = new PlayerWrapper(player);
		var wrappedLevel = new LevelWrapper(level);
		var wrappedPos = new BlockPosWrapper(blockPos);

		int result = mod.getTreeHandler().fastBreakTree(wrappedLevel, wrappedPlayer, wrappedPos);
		if ((result & BreakTreeResult.FLAG_ERROR) == 0) {
			BreakMode mode = BreakMode.ofOrdinal(result & ~BreakTreeResult.FLAG_CANCELLED);
			if (mode == BreakMode.INSTANTANEOUS) {
				return (result & BreakTreeResult.FLAG_CANCELLED) == 0;
			} else if (mode == BreakMode.SHIFT_DOWN) {
				return false;
			} else {
				throw new AssertionError();
			}
		} else if ((result & ~(BreakTreeResult.FLAG_CANCELLED | BreakTreeResult.FLAG_ERROR)) == BreakTreeResult.ERROR_ABSENT_TOOL) {
			return false;
		} else {
			return true;
		}
	}
}
