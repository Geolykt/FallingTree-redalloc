package fr.raksrinana.fallingtree.forge.event;

import fr.raksrinana.fallingtree.common.FallingTreeCommon;
import fr.raksrinana.fallingtree.common.config.enums.BreakMode;
import fr.raksrinana.fallingtree.common.tree.BreakTreeResult;
import fr.raksrinana.fallingtree.forge.common.wrapper.BlockPosWrapper;
import fr.raksrinana.fallingtree.forge.common.wrapper.LevelWrapper;
import fr.raksrinana.fallingtree.forge.common.wrapper.PlayerWrapper;
import lombok.RequiredArgsConstructor;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class BlockBreakListener{
	@NotNull
	private final FallingTreeCommon<?> mod;
	
	@SubscribeEvent
	public void onBreakSpeed(@Nonnull PlayerEvent.BreakSpeed event){
		if(event.isCanceled()){
			return;
		}
		
		var optionalPos = event.getPosition();
		if(optionalPos.isEmpty()){
			return;
		}
		
		var wrappedPlayer = new PlayerWrapper(event.getEntity());
		var wrappedPos = new BlockPosWrapper(optionalPos.get());
		
		var result = mod.getTreeHandler().getBreakSpeed(wrappedPlayer, wrappedPos, event.getNewSpeed());
		if(result.isEmpty()){
			return;
		}
		
		event.setNewSpeed(result.get());
	}
	
	@SubscribeEvent
	public void onBlockBreakEvent(@Nonnull BlockEvent.BreakEvent event){
		if(event.isCanceled()){
			return;
		}
		if(event instanceof FallingTreeBlockBreakEvent){
			return;
		}
		
		var wrappedPlayer = new PlayerWrapper(event.getPlayer());
		var wrappedLevel = new LevelWrapper(event.getLevel());
		var wrappedPos = new BlockPosWrapper(event.getPos());

		int result = mod.getTreeHandler().fastBreakTree(wrappedLevel, wrappedPlayer, wrappedPos);
		if ((result & BreakTreeResult.FLAG_ERROR) == 0) {
			BreakMode mode = BreakMode.ofOrdinal(result & ~BreakTreeResult.FLAG_CANCELLED);
			if (mode == BreakMode.INSTANTANEOUS) {
				event.setCanceled((result & BreakTreeResult.FLAG_CANCELLED) != 0);
			} else if (mode == BreakMode.SHIFT_DOWN) {
				event.setCanceled(true);
			} else {
				throw new AssertionError();
			}
		} else if ((result & ~(BreakTreeResult.FLAG_CANCELLED | BreakTreeResult.FLAG_ERROR)) == BreakTreeResult.ERROR_ABSENT_TOOL) {
			if (event.isCancelable()) {
				event.setCanceled(true);
			}
		}
	}
}
