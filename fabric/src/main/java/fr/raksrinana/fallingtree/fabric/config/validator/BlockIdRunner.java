package fr.raksrinana.fallingtree.fabric.config.validator;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class BlockIdRunner implements ValidatorRunner<BlockId>{
	private static final Pattern MINECRAFT_ID_PATTERN = Pattern.compile("#?[a-z0-9_.-]+:[a-z0-9/._-]+");
	private static final Component errorText = new TranslatableComponent("text.autoconfig.fallingtree.error.invalidBlockResourceLocation");
	
	@Override
	public Optional<Component> apply(Object value, BlockId annotation){
		if(value == null){
			return Optional.of(errorText);
		}
		if(value instanceof String){
			String val = value.toString();
			if(annotation.allowEmpty() && val.isEmpty()){
				// OK
			}
			else{
				boolean valid = MINECRAFT_ID_PATTERN.matcher((String) value).matches();
				if(!valid){
					return Optional.of(errorText);
				}
			}
		}
		else if(value instanceof List){
			List<?> list = (List<?>) value;
			boolean valid = list.stream()
					.filter(Objects::nonNull)
					.map(Object::toString)
					.allMatch(val -> MINECRAFT_ID_PATTERN.matcher(val).matches());
			if(!valid){
				return Optional.of(errorText);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public Class<BlockId> getAnnotationClass(){
		return BlockId.class;
	}
}
