package ir.hatamiarash.hyperonline.interfaces;

import android.content.Context;
import android.support.annotation.NonNull;

public interface Analytics {
	void init(@NonNull final Context context);
	
	void reportScreen(@NonNull final String name);
	
	void reportAction(@NonNull final String category, @NonNull final String name);
	
	void reportEvent(@NonNull String event);
	
	void reportCard(@NonNull String id, @NonNull String name, @NonNull String price);
	
	void reportPurchase(@NonNull String id, @NonNull String name, @NonNull String price, boolean status);
	
	void reportStartCheckout(int count, int price);
}