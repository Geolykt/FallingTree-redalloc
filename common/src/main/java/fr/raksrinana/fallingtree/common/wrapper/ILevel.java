package fr.raksrinana.fallingtree.common.wrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ILevel extends IWrapper{
	@NotNull
	IChunk getChunk(@NotNull IBlockPos blockPos);
	
	boolean hasChunk(int x, int z);
	
	@NotNull
	IRandomSource getRandom();
	
	boolean isServer();
	
	@NotNull
	IBlockState getBlockState(@NotNull IBlockPos blockPos);
	
	@Nullable
	IBlockEntity getBlockEntity(@NotNull IBlockPos blockPos);
	
	boolean removeBlock(@NotNull IBlockPos blockPos, boolean b);
}
