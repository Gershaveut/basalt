package org.gershaveut.basalt.view;

import com.javadocking.component.DefaultSwComponentFactory;

public class ApplicationSwComponentFactory extends DefaultSwComponentFactory {
    public ApplicationSwComponentFactory() {
        setPopupMenuFactory(new ApplicationPopupMenuFactory());
    }
}
