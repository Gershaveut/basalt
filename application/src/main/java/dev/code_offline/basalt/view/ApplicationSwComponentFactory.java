package dev.code_offline.basalt.view;

import com.javadocking.component.DefaultSwComponentFactory;

public class ApplicationSwComponentFactory extends DefaultSwComponentFactory {
    public ApplicationSwComponentFactory() {
        setPopupMenuFactory(new ApplicationPopupMenuFactory());
    }
}
