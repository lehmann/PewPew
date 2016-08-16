package com.lehmann.pewpew;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class TablePreferences extends PreferenceActivity {
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.addPreferencesFromResource(R.xml.preferences);
	}
}
