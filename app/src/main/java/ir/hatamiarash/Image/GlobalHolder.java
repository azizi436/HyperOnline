/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.Image;

public class GlobalHolder {

    private PickerManager pickerManager;

    private static GlobalHolder ourInstance = new GlobalHolder();

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
