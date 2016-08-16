package com.lehmann.pewpew;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

/**
 * Activity com o About do jogo
 * @author limao
 *
 */
public class AboutActivity extends Activity {

	public static Intent startForLevel(final Context context, final int level) {
		final Intent aboutIntent = new Intent(context, AboutActivity.class);
		aboutIntent.putExtra("level", level);
		context.startActivity(aboutIntent);
		return aboutIntent;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.about);

		final String baseText = this.getString(R.string.about_text);
		String tableRulesText = null;
		try {
			final String fieldName = "table"
					+ this.getIntent().getIntExtra("level", 1) + "_rules";
			final int tableRulesID = (Integer) R.string.class.getField(
					fieldName).get(null);
			tableRulesText = this.getString(tableRulesID);
		} catch (final Exception ex) {
			tableRulesText = null;
		}
		if (tableRulesText == null) {
			tableRulesText = "";
		}
		final String displayText = baseText.replace("[TABLE_RULES]",
				tableRulesText);

		final TextView tv = (TextView) this.findViewById(R.id.aboutTextView);
		tv.setText(displayText);
	}

}
