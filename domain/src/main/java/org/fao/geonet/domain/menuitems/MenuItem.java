/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.domain.menuitems;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.fao.geonet.domain.GeonetEntity;

/**
 * A page with content and properties
 */
@Entity(name = "SPG_MenuItem")
@Table(name = "SPG_MenuItem",
       uniqueConstraints=
       @UniqueConstraint(columnNames={"language", "name"}) )
@SequenceGenerator(name = MenuItem.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class MenuItem extends GeonetEntity implements Serializable {
    static final String ID_SEQ_NAME = "menuitem_id_seq";

    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier of the content
     */
    private long id;
    /**
     * The 3 letters language of the element
     */
    private String language;
    /**
     * The name of the menuItem, used in the URL
     */
    private String name;
    /**
     * The raw content of the page (local page)
     */
    private byte[] data;
    /**
     * To call an external link (remote url)
     */
    private String link;
    /**
     * Content type
     */
    private ContentType contentType;
    /**
     * Sections where the menuItem is shown
     */
    private List<MenuSection> sections;
    /**
     * Status of the page
     */
    private MenuItemStatus status;

    public MenuItem() {

    }

    public MenuItem(long id, String language, String name, byte[] data, String link, ContentType contentType, List<MenuSection> sections, MenuItemStatus status) {
        super();
        this.id = id;
        this.language = language;
        this.name = name;
        this.data = data;
        this.link = link;
        this.contentType = contentType;
        this.sections = sections;
        this.status = status;
    }

    public enum MenuItemStatus {
        PUBLIC, PUBLIC_ONLY, PRIVATE, HIDDEN;
    }

    public enum ContentType {
        LINK, HTML, TEXT, MARKDOWN, WIKI;
    }

    // These are the sections where is shown the link to the Page object
    public enum MenuSection {
        ALL, TOP, FOOTER, MENU, SUBMENU, CUSTOM_MENU1, CUSTOM_MENU2, CUSTOM_MENU3, DRAFT;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(nullable = false)
    public long getId() {
        return id;
    }

    @Column
    public String getLanguage() {
        return language;
    }

    @Column
    public String getName() {
        return name;
    }

    @Column
    @Nullable
    @Lob
    @Basic(fetch = FetchType.LAZY)
    public byte[] getData() {
        return data;
    }

    @Column
    @Nullable
    @Basic(fetch = FetchType.LAZY)
    public String getLink() {
        return link;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public ContentType getContentType() {
        return contentType;
    }

    @ElementCollection(targetClass = MenuSection.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "SPG_Sections")
    @Column(name = "section")
    public List<MenuSection> getSections() {
        return sections;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public MenuItemStatus getStatus() {
        return status;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public void setSections(List<MenuSection> sections) {
        this.sections = sections;
    }

    public void setStatus(MenuItemStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), this.getName());
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setName(String name) {
        this.name = name;
    }

}
