/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.helpers;

import java.text.DecimalFormat;

public class PriceHelper {
	public static String formatPrice(String price) {
		DecimalFormat df = new DecimalFormat("###,###");
		return df.format(Integer.valueOf(price));
	}
}
