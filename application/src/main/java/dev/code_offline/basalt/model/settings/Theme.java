package dev.code_offline.basalt.model.settings;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.*;
import dev.code_offline.basalt.ApplicationUtil;

public enum Theme {
	WHITE(FlatLightLaf::setup),
	DARK(FlatDarkLaf::setup),
	ARC(FlatArcIJTheme::setup),
	ONE_DARK(FlatOneDarkIJTheme::setup),
	GRUVBOX_DARK_HARD(FlatGruvboxDarkHardIJTheme::setup),
	NORD(FlatNordIJTheme::setup),
	HIGH_CONTRAST(FlatHighContrastIJTheme::setup);
	
	private final Runnable theme;
	
	Theme(Runnable theme) {
		this.theme = theme;
	}
	
	public void applyTheme() {
		theme.run();
		FlatLaf.updateUILater();
	}
	
	@Override
	public String toString() {
		return ApplicationUtil.toDisplayName(super.toString());
	}
}
