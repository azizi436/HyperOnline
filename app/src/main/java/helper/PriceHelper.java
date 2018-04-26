package helper;

import java.text.DecimalFormat;

public class PriceHelper {
	public static String formatPrice(String price) {
		DecimalFormat df = new DecimalFormat("###,###");
		return df.format(Integer.valueOf(price));
	}
}
