/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package helper;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import ir.hatamiarash.hyperonline.R;

public class CustomPrimaryDrawerItem extends PrimaryDrawerItem {
	@Override
	public int getLayoutRes() {
		return R.layout.custom_material_drawer_item_primary;
	}
	
	@Override
	public int getType() {
		return R.id.material_drawer_item_primary_custom;
	}
}