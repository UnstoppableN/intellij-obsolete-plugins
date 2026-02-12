package com.testapp.components;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.annotations.Property;

public class TapestryAttrCaseInsensitive {

    @Component(id = "myGrid")
    private Grid myGrid;

    @Property
    private Object gridModel;

    public Object getGridModel() {
        return gridModel;
    }

    public void setGridModel(Object gridModel) {
        this.gridModel = gridModel;
    }
}
