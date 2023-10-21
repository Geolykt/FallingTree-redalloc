package fr.raksrinana.fallingtree.common.config.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BreakMode{
	INSTANTANEOUS(true, true),
	SHIFT_DOWN(false, false);
	
	private final boolean checkLeavesAround;
	private final boolean applySpeedMultiplier;

	// #values creates a clone of the array - we do not want that for performance reasons. Hence this field exists.
	private static final BreakMode[] valuesCache = BreakMode.values();

	public static BreakMode ofOrdinal(int ordinal) {
		return BreakMode.valuesCache[ordinal];
	}
}
