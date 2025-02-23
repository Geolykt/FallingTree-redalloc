package fr.raksrinana.fallingtree.common.config.real;

import com.google.gson.annotations.Expose;
import fr.raksrinana.fallingtree.common.config.IEnchantmentConfiguration;
import lombok.Data;

@Data
public class EnchantmentConfiguration implements IEnchantmentConfiguration{
	@Expose
	private boolean registerEnchant = false;
	@Expose
	private boolean registerSpecificEnchant = false;
	@Expose
	private boolean hideEnchant = false;
}
