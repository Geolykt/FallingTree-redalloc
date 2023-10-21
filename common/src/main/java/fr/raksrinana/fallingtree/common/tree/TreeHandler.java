package fr.raksrinana.fallingtree.common.tree;

import fr.raksrinana.fallingtree.common.FallingTreeCommon;
import fr.raksrinana.fallingtree.common.config.enums.BreakMode;
import fr.raksrinana.fallingtree.common.tree.breaking.BreakTreeTooBigException;
import fr.raksrinana.fallingtree.common.tree.breaking.ITreeBreakingHandler;
import fr.raksrinana.fallingtree.common.tree.breaking.InstantaneousTreeBreakingHandler;
import fr.raksrinana.fallingtree.common.tree.breaking.ShiftDownTreeBreakingHandler;
import fr.raksrinana.fallingtree.common.tree.builder.TreeTooBigException;
import fr.raksrinana.fallingtree.common.tree.exception.NoTreeFoundException;
import fr.raksrinana.fallingtree.common.tree.exception.NotServerException;
import fr.raksrinana.fallingtree.common.tree.exception.PlayerNotInRightState;
import fr.raksrinana.fallingtree.common.tree.exception.ToolUseForcedException;
import fr.raksrinana.fallingtree.common.tree.exception.TreeBreakingException;
import fr.raksrinana.fallingtree.common.tree.exception.TreeBreakingNotEnabledException;
import fr.raksrinana.fallingtree.common.utils.CacheSpeed;
import fr.raksrinana.fallingtree.common.wrapper.IBlockPos;
import fr.raksrinana.fallingtree.common.wrapper.IEnchantment;
import fr.raksrinana.fallingtree.common.wrapper.IItemStack;
import fr.raksrinana.fallingtree.common.wrapper.ILevel;
import fr.raksrinana.fallingtree.common.wrapper.IPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.Objects.isNull;

@Log4j2
@RequiredArgsConstructor
public class TreeHandler{
	@NotNull
	private final FallingTreeCommon<?> mod;
	private final Map<UUID, CacheSpeed> speedCache = new ConcurrentHashMap<>();

	@NotNull
	public int fastBreakTree(@NotNull ILevel level, @NotNull IPlayer player, @NotNull IBlockPos blockPos) {
		if(!level.isServer()){
			return BreakTreeResult.FLAG_ERROR | BreakTreeResult.ERROR_NOT_SERVER;
		}
		if(!mod.getConfiguration().getTrees().isTreeBreaking()){
			return BreakTreeResult.FLAG_ERROR | BreakTreeResult.ERROR_NOT_ENABLED;
		}

		if(!mod.checkForceToolUsage(player, level, blockPos)){
			mod.notifyPlayer(player, mod.translate("chat.fallingtree.force_tool_usage", mod.getConfiguration().getTrees().getMaxScanSize()));
			return BreakTreeResult.FLAG_ERROR | BreakTreeResult.ERROR_ABSENT_TOOL;
		}

		if(!mod.isPlayerInRightState(player)){
			return BreakTreeResult.FLAG_ERROR | BreakTreeResult.ERROR_WRONG_PLAYER_STATE;
		}

		try{
			var treeOptional = mod.getTreeBuilder().getTree(player, level, blockPos);
			if(treeOptional.isEmpty()){
				return BreakTreeResult.FLAG_ERROR | BreakTreeResult.ERROR_ABSENT_TREE;
			}

			var tree = treeOptional.get();
			var breakMode = getBreakMode(player.getMainHandItem());
			var result = getBreakingHandler(breakMode).breakTree(player, tree);
			return (result ? 0 : BreakTreeResult.FLAG_CANCELLED) | breakMode.ordinal();
		}
		catch(TreeTooBigException e){
			mod.notifyPlayer(player, mod.translate("chat.fallingtree.tree_too_big", mod.getConfiguration().getTrees().getMaxScanSize()));
			return BreakTreeResult.ERROR_TREE_TOO_LARGE;
		}
		catch(BreakTreeTooBigException e){
			mod.notifyPlayer(player, mod.translate("chat.fallingtree.break_tree_too_big", mod.getConfiguration().getTrees().getMaxSize()));
			return BreakTreeResult.ERROR_TREE_TOO_LARGE;
		}
	}

	@NotNull
	@Deprecated
	public BreakTreeResult breakTree(@NotNull ILevel level, @NotNull IPlayer player, @NotNull IBlockPos blockPos) throws TreeBreakingNotEnabledException, PlayerNotInRightState, ToolUseForcedException, TreeBreakingException, NoTreeFoundException, NotServerException{
		return BreakTreeResult.decode(this.fastBreakTree(level, player, blockPos));
	}
	
	@NotNull
	private BreakMode getBreakMode(@NotNull IItemStack itemStack){
		return itemStack.getAnyEnchant(mod.getChopperEnchantments())
				.flatMap(IEnchantment::getBreakMode)
				.orElseGet(() -> mod.getConfiguration().getTrees().getBreakMode());
	}
	
	@NotNull
	private ITreeBreakingHandler getBreakingHandler(@NotNull BreakMode breakMode){
		return switch(breakMode){
			case INSTANTANEOUS -> InstantaneousTreeBreakingHandler.getInstance(mod);
			case SHIFT_DOWN -> ShiftDownTreeBreakingHandler.getInstance(mod);
		};
	}
	
	@NotNull
	public Optional<Float> getBreakSpeed(@NotNull IPlayer player, @NotNull IBlockPos blockPos, float originalSpeed){
		if(!mod.getConfiguration().getTrees().isTreeBreaking()){
			return Optional.empty();
		}
		if(!getBreakMode(player.getMainHandItem()).isApplySpeedMultiplier()){
			return Optional.empty();
		}
		if(!mod.isPlayerInRightState(player)){
			return Optional.empty();
		}
		
		var cacheSpeed = speedCache.compute(player.getUUID(), (uuid, speed) -> {
			if(isNull(speed) || !speed.isValid(blockPos)){
				speed = getSpeed(player, blockPos, originalSpeed);
			}
			return speed;
		});
		return Optional.ofNullable(cacheSpeed).map(CacheSpeed::getSpeed);
	}
	
	@Nullable
	private CacheSpeed getSpeed(@NotNull IPlayer player, @NotNull IBlockPos pos, float originalSpeed){
		var speedMultiplicand = mod.getConfiguration().getTools().getSpeedMultiplicand();
		try{
			return speedMultiplicand <= 0 ? null :
					mod.getTreeBuilder().getTree(player, player.getLevel(), pos)
							.map(tree -> new CacheSpeed(pos, originalSpeed / ((float) speedMultiplicand * tree.getLogCount())))
							.orElse(null);
		}
		catch(TreeTooBigException e){
			return null;
		}
	}
}
