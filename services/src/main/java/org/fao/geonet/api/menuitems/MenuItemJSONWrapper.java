package org.fao.geonet.api.menuitems;

import java.io.Serializable;
import java.util.List;

import org.fao.geonet.domain.menuitems.MenuItem;
import org.fao.geonet.domain.menuitems.MenuItem.ContentType;
import org.fao.geonet.domain.menuitems.MenuItem.MenuItemStatus;
import org.fao.geonet.domain.menuitems.MenuItem.MenuSection;

// Wrapper to filter the fields shown on JSON
public class MenuItemJSONWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    private MenuItem item;

    public MenuItemJSONWrapper(MenuItem p) {
        item = p;
    }

    public Long getId() {
        return item.getId();
    }

    public String getName() {
        return item.getName();
    }

    public String getLanguage() {
        return item.getLanguage();
    }

    public ContentType getContentType() {
        return item.getContentType();
    }

    public String getLink() {
        return item.getLink();
    }

    public List<MenuSection> getSections() {
        return item.getSections();
    }

    public MenuItemStatus getStatus() {
        return item.getStatus();
    }

    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getName());
    }

}
