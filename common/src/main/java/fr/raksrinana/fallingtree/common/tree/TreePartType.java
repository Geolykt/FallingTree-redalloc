package fr.raksrinana.fallingtree.common.tree;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TreePartType{
	LEAF(false),
	LEAF_NEED_BREAK(true),
	LOG(true),
	NETHER_WART(true),
	MANGROVE_ROOTS(true),
	OTHER(false);
	
	private final boolean breakable;
}
