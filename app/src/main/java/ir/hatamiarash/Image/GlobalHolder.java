/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.Image;

import org.jetbrains.annotations.Contract;

public class GlobalHolder {
	
	private PickerManager pickerManager;
	
	private static GlobalHolder ourInstance = new GlobalHolder();
	
	@Contract(pure = true)
	public static GlobalHolder getInstance() {
		return ourInstance;
	}
	
	private GlobalHolder() {
	}
	
	
	public PickerManager getPickerManager() {
		return pickerManager;
	}
	
	public void setPickerManager(PickerManager pickerManager) {
		this.pickerManager = pickerManager;
	}
}
