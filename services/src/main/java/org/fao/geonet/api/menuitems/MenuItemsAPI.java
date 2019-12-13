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
package org.fao.geonet.api.menuitems;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.exception.WebApplicationException;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.menuitems.MenuItem;
import org.fao.geonet.repository.menuitems.MenuItemRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.UserSession;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping(value = { "/{portal}/api/menuitems", "/{portal}/api/" + API.VERSION_0_1 + "/menuitems" })
@Api(value = "menuitems", tags = "menuitems", description = "GeoNetwork catalog menu items")
@Controller("menuitems")
public class MenuItemsAPI {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @ApiOperation(value = "Add a new menu item in DRAFT section in status HIDDEN", notes = "<p>Is not possible to load a link and a file at the same time.</p> <a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-menu-item/define-menuitems.html'>More info</a>", nickname = "addMenuItem")
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = MENUITEM_SAVED),
            @ApiResponse(code = 404, message = MENUITEM_NOT_FOUND),
            @ApiResponse(code = 400, message = ERROR_CREATE),
            @ApiResponse(code = 409, message = MENUITEM_DUPLICATE),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT),
            @ApiResponse(code = 500, message = ERROR_FILE) })
    @PreAuthorize("hasRole('Administrator')")
    public void addMenuItem(
            @RequestParam(value = "language", required = true) final String language,
            @RequestParam(value = "name", required = true) final String name,
            @RequestParam(value = "file", required = false) final MultipartFile file,
            @RequestParam(value = "data", required = false) final String data,
            @RequestParam(value = "link", required = false) final String link,
            @RequestParam(value = "format", required = true) final MenuItem.ContentType format,
            @ApiIgnore final HttpServletResponse response) throws ResourceAlreadyExistException {

        try {
            byte[] content = extractTheContent(file, data);

            checkMandatoryContent(content, link);

            checkUniqueContent(content, link);

            checkCorrectFormat(content, link, format);

            if (menuItemRepository.findOneByLanguageAndName(language, name) == null) {

                MenuItem item = getEmptyHiddenDraftMenuItem(language, name, format);

                fillContent(language, name, content, link, item);

                menuItemRepository.save(item);

            } else {
                throw new ResourceAlreadyExistException();
            }
        } catch (ResourceAlreadyExistException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            Log.error(Log.WEBAPP, "Error", e);
            throw e;
        }
    }

    // Isn't done with PUT because the multipart support as used by Spring
    // doesn't support other request method then POST
    @ApiOperation(value = "Edit a menu item content and format", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-menu-item/define-menuitems.html'>More info</a>", nickname = "editMenuItem")
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = MENUITEM_UPDATED),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @PreAuthorize("hasRole('Administrator')")
    public void editMenuItem(
            @PathVariable(value = "id") final Long id,
            @RequestParam(value = "language", required = false) final String language,
            @RequestParam(value = "name", required = false) final String name,
            @RequestParam(value = "file", required = false) final MultipartFile file,
            @RequestParam(value = "data", required = false) final String data,
            @RequestParam(value = "link", required = false) final String link,
            @RequestParam(value = "format", required = true) final MenuItem.ContentType format,
            @ApiIgnore final HttpServletResponse response) throws ResourceNotFoundException, ResourceAlreadyExistException {

        try {
            final MenuItem item = searchMenuItem(id);

            byte[] content = extractTheContent(file, data);

            checkUniqueContent(content, link);

            checkCorrectFormat(content, link, format);

            checkUniqueLanguageName(language, name, item);

            fillContent(language, name, content, link, item);

            item.setContentType(format);
            menuItemRepository.save(item);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (ResourceAlreadyExistException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            Log.error(Log.WEBAPP, "Error", e);
            throw e;
        }
    }


    @ApiOperation(value = "Delete a menu item ", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-menu-item/define-menuitems.html'>More info</a>", nickname = "deleteMenuItem")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = MENUITEM_DELETED),
            @ApiResponse(code = 404, message = MENUITEM_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @PreAuthorize("hasRole('Administrator')")
    public void deleteMenuItem(
            @PathVariable(value = "id") final Long id,
            @ApiIgnore final HttpServletResponse response) throws ResourceNotFoundException {

        try {
            searchMenuItem(id);

            menuItemRepository.delete(id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            Log.error(Log.WEBAPP, "Error", e);
            throw e;
        }
    }


    @ApiOperation(value = "Return the menu item details except the content", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-menu-item/define-menuitems.html'>More info</a>", nickname = "getMenuItemFromLanguageAndName")
    @RequestMapping(value = "/{language}/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = MENUITEM_OK),
            @ApiResponse(code = 404, message = MENUITEM_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW) })
    @ResponseBody
    public ResponseEntity<MenuItemJSONWrapper> getMenuItem(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "name") final String name,
            @ApiIgnore final HttpServletResponse response,
            @ApiIgnore final HttpSession session) throws ResourceNotFoundException {

        try {
        final MenuItem item = searchMenuItem(language, name);

        return checkPermissionsOnMenuItemAndReturn(session, item);
    } catch (ResourceNotFoundException e) {
        throw e;
    } catch (IllegalArgumentException e) {
        throw e;
    } catch (Exception e) {
        Log.error(Log.WEBAPP, "Error", e);
        throw e;
    }
    }

    @ApiOperation(value = "Return the menu item details except the content", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-menu-item/define-menuitems.html'>More info</a>", nickname = "getMenuItemFromId")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = MENUITEM_OK),
            @ApiResponse(code = 404, message = MENUITEM_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW) })
    @ResponseBody
    public ResponseEntity<MenuItemJSONWrapper> getMenuItem(
            @PathVariable(value = "id") final Long id,
            @ApiIgnore final HttpServletResponse response,
            @ApiIgnore final HttpSession session) throws ResourceNotFoundException {

try {
        final MenuItem item = searchMenuItem(id);

        return checkPermissionsOnMenuItemAndReturn(session, item);
    } catch (ResourceNotFoundException e) {
        throw e;
    } catch (IllegalArgumentException e) {
        throw e;
    } catch (Exception e) {
        Log.error(Log.WEBAPP, "Error", e);
        throw e;
    }
    }


    @ApiOperation(value = "Return the static html content identified by id", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-menu-item/define-menuitems.html'>More info</a>", nickname = "getMenuItemContent")
    @RequestMapping(value = "/{language}/{name}/content", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    @ApiResponses(value = { @ApiResponse(code = 200, message = MENUITEM_OK),
            @ApiResponse(code = 404, message = MENUITEM_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW) })
    @ResponseBody
    public ResponseEntity<String> getPageContent(
            @PathVariable(value = "language") final String language,
            @PathVariable(value = "name") final String name,
            @ApiIgnore final HttpServletResponse response,
            @ApiIgnore final HttpSession session) throws ResourceNotFoundException {

try {
        final MenuItem item = searchMenuItem(language, name);

        if (item == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            final UserSession us = ApiUtils.getUserSession(session);
            if (item.getStatus().equals(MenuItem.MenuItemStatus.HIDDEN) && us.getProfile() != Profile.Administrator) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else if (item.getStatus().equals(MenuItem.MenuItemStatus.PRIVATE) && (us.getProfile() == null || us.getProfile() == Profile.Guest)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                String content = "";
                if (item.getData() != null && item.getData().length > 0) {
                    try {
                        content = new String(item.getData(), "UTF-8");
                    } catch (final UnsupportedEncodingException e) {
                        content = new String(item.getData());
                    }
                } else {
                    content = item.getLink();
                }

                return new ResponseEntity<>(content, HttpStatus.OK);
            }
        }
    } catch (ResourceNotFoundException e) {
        throw e;
    } catch (IllegalArgumentException e) {
        throw e;
    } catch (Exception e) {
        Log.error(Log.WEBAPP, "Error", e);
        throw e;
    }
    }


    @ApiOperation(value = "Adds the menu item to a section. This means that the link to the page will be shown in the list associated to that section.", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-menu-item/define-menuitems.html'>More info</a>", nickname = "addMenuItemToSection")
    @RequestMapping(value = "/{id}/{section}", method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = MENUITEM_UPDATED),
            @ApiResponse(code = 404, message = MENUITEM_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @PreAuthorize("hasRole('Administrator')")
    public void addMenuItemToSection(
            @PathVariable(value = "id") final Long id,
            @PathVariable(value = "section") final MenuItem.MenuSection section,
            @ApiIgnore final HttpServletResponse response) throws ResourceNotFoundException {

        try {
        final MenuItem item = searchMenuItem(id);

        final MenuItem.MenuSection sectionToAdd = section;

        if (sectionToAdd.equals(MenuItem.MenuSection.ALL)) {
            item.setSections(new ArrayList<MenuItem.MenuSection>());
            item.getSections().add(sectionToAdd);
        } else if (!item.getSections().contains(sectionToAdd)) {
            item.getSections().add(sectionToAdd);
        }

        menuItemRepository.save(item);
    } catch (ResourceNotFoundException e) {
        throw e;
    } catch (IllegalArgumentException e) {
        throw e;
    } catch (Exception e) {
        Log.error(Log.WEBAPP, "Error", e);
        throw e;
    }
    }


    @ApiOperation(value = "Removes the menu item from a section. This means that the link to the page will not be shown in the list associated to that section.", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-menu-item/define-menuitems.html'>More info</a>", nickname = "removeMenuItemFromSection")
    @RequestMapping(value = "/{id}/{section}", method = RequestMethod.DELETE)
    @ApiResponses(value = { @ApiResponse(code = 200, message = MENUITEM_UPDATED),
            @ApiResponse(code = 404, message = MENUITEM_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @PreAuthorize("hasRole('Administrator')")
    public void removeMenuItemFromSection(
            @PathVariable(value = "id") final Long id,
            @PathVariable(value = "section") final MenuItem.MenuSection section,
            @ApiIgnore final HttpServletResponse response) throws ResourceNotFoundException {

        try {
        final MenuItem item = searchMenuItem(id);

        if (section.equals(MenuItem.MenuSection.ALL)) {
            item.setSections(new ArrayList<MenuItem.MenuSection>());
            item.getSections().add(MenuItem.MenuSection.DRAFT);
        } else if (section.equals(MenuItem.MenuSection.DRAFT)) {
            // Cannot remove a item from DRAFT section
        } else if (item.getSections().contains(section)) {
            item.getSections().remove(section);
        }

        menuItemRepository.save(item);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            Log.error(Log.WEBAPP, "Error", e);
            throw e;
        }
    }


    @ApiOperation(value = "Changes the status of a menu item.", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-menu-item/define-menuitems.html'>More info</a>", nickname = "changeMenuItemStatus")
    @RequestMapping(value = "/{id}/{status}", method = RequestMethod.PUT)
    @ApiResponses(value = { @ApiResponse(code = 200, message = MENUITEM_UPDATED),
            @ApiResponse(code = 404, message = MENUITEM_NOT_FOUND),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    @PreAuthorize("hasRole('Administrator')")
    public void changeMenuItemStatus(
            @PathVariable(value = "id") final Long id,
            @PathVariable(value = "status") final MenuItem.MenuItemStatus status,
            @ApiIgnore final HttpServletResponse response) throws ResourceNotFoundException {

        try {
        final MenuItem item = searchMenuItem(id);

        item.setStatus(status);

        menuItemRepository.save(item);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            Log.error(Log.WEBAPP, "Error", e);
            throw e;
        }
    }


    @ApiOperation(value = "List all menu items according to the filters", notes = "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/define-menu-item/define-menuitems.html'>More info</a>", nickname = "listMenuItems")
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = { @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW) })
    @ResponseBody
    public ResponseEntity<List<MenuItemJSONWrapper>> listMenuItems(
            @RequestParam(value = "language", required = false) final String language,
            @RequestParam(value = "section", required = false) final MenuItem.MenuSection section,
            @ApiIgnore final HttpServletResponse response,
            @ApiIgnore final HttpSession session) {

        try {
        final UserSession us = ApiUtils.getUserSession(session);
        List<MenuItem> unfilteredResult = null;

        if (language == null) {
            unfilteredResult = menuItemRepository.findAll();
        } else {
            unfilteredResult = menuItemRepository.findByLanguage(language);
        }

        final List<MenuItemJSONWrapper> filteredResult = new ArrayList<>();

        for (final MenuItem item : unfilteredResult) {
            if (item.getStatus().equals(MenuItem.MenuItemStatus.HIDDEN) && us.getProfile() == Profile.Administrator
                    || item.getStatus().equals(MenuItem.MenuItemStatus.PRIVATE) && us.getProfile() != null && us.getProfile() != Profile.Guest
                    || item.getStatus().equals(MenuItem.MenuItemStatus.PUBLIC)
                    || item.getStatus().equals(MenuItem.MenuItemStatus.PUBLIC_ONLY) && !us.isAuthenticated()) {
                if (section == null || MenuItem.MenuSection.ALL.equals(section)) {
                    filteredResult.add(new MenuItemJSONWrapper(item));
                } else {
                    final List<MenuItem.MenuSection> sections = item.getSections();
                    final boolean containsALL = sections.contains(MenuItem.MenuSection.ALL);
                    final boolean containsRequestedSection = sections.contains(section);
                    if (containsALL || containsRequestedSection) {
                        filteredResult.add(new MenuItemJSONWrapper(item));
                    }
                }
            }
        }

        return new ResponseEntity<>(filteredResult, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            Log.error(Log.WEBAPP, "Error", e);
            throw e;
        }
    }


    /* Local Utility and constants */

    /**
     * Extract the content
        * @param file
        * @param data
        * @return
     */
    private byte[] extractTheContent(final MultipartFile file, final String data) throws WebApplicationException {
        if(!(file == null || file.isEmpty()) && !StringUtils.isBlank(data)) {
            throw new IllegalArgumentException("Cannot load content from a file and text ant the same time.");
        }

        if(!(file == null || file.isEmpty())) {
            checkFileType(file);
            try {
                return file.getBytes();
            } catch (final Exception e) {
                // Wrap into managed exception
                throw new WebApplicationException(e);
            }

        } else if(!StringUtils.isBlank(data)) {
            return data.getBytes();
        }

        return null;
    }

    /**
     * Check correct format.
     *
     * @param data the data
     * @param link the link
     * @param format the format
     */
    private void checkCorrectFormat(final byte[] data, final String link, final MenuItem.ContentType format) throws IllegalArgumentException {
        // Cannot set format to LINK and upload a file
        if (MenuItem.ContentType.LINK.equals(format) && data != null) {
            throw new IllegalArgumentException("Wrong format.");
        }

        // Cannot set a link without setting format to LINK
        if (!MenuItem.ContentType.LINK.equals(format) && !StringUtils.isBlank(link)) {
            throw new IllegalArgumentException("Wrong format.");
        }
    }

    /**
     * Check that there are not existing items with same language/name
        * @param language
        * @param name
        * @param item
        * @throws ResourceAlreadyExistException
     */
    private void checkUniqueLanguageName(final String language, final String name, final MenuItem item)
            throws ResourceAlreadyExistException {
        String updatedLanguage = StringUtils.isBlank(language) ? item.getLanguage() : language;
        String updatedName = StringUtils.isBlank(name) ? item.getName() : name;

        if (!updatedLanguage.equals(item.getLanguage()) || !updatedName.equals(item.getName())) {

            MenuItem newMenuItem = menuItemRepository.findOneByLanguageAndName(updatedLanguage, updatedName);
            if (newMenuItem != null) {
                throw new ResourceAlreadyExistException();
            }
        }
    }

    /**
     * Check that link or a file is defined.
     *
     * @param data the data
     * @param link the link
     */
    private void checkMandatoryContent(final byte[] data, final String link) throws IllegalArgumentException {
        // Cannot set both: link and file
        if (StringUtils.isBlank(link) && data == null) {
            throw new IllegalArgumentException("A content associated to the menu item, a link or a file, is mandatory.");
        }
    }

    /**
     * Check unique content.
     *
     * @param data the data
     * @param link the link
     */
    private void checkUniqueContent(final byte[] data, final String link) throws IllegalArgumentException {
        // Cannot set both: link and file
        if (!StringUtils.isBlank(link) && data != null) {
            throw new IllegalArgumentException("A content associated to the menu item, a link or a file, is mandatory. But is not possible to associate both to the same menu item.");
        }
    }

    /**
     * Check file type.
     *
     * @param data the data
     */
    private void checkFileType(final MultipartFile data) throws MultipartException {
        if (data != null) {
            String extension = FilenameUtils.getExtension(data.getOriginalFilename());
            final String[] supportedExtensions = { "html", "HTML", "txt", "TXT", "md", "MD" };

            if (!ArrayUtils.contains(supportedExtensions, extension)) {
                throw new MultipartException("Unsuppoted file type (only html, txt and md are allowed).");
            }
        }
    }

    /**
     * Check permissions on single item and return.
     *
     * @param session the session
     * @param item the Menu item
     * @return the response entity
     */
    private ResponseEntity<MenuItemJSONWrapper> checkPermissionsOnMenuItemAndReturn(final HttpSession session, final MenuItem item) {
        if (item == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            final UserSession us = ApiUtils.getUserSession(session);
            if (item.getStatus().equals(MenuItem.MenuItemStatus.HIDDEN) && us.getProfile() != Profile.Administrator) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else if (item.getStatus().equals(MenuItem.MenuItemStatus.PRIVATE) && (us.getProfile() == null || us.getProfile() == Profile.Guest)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                return new ResponseEntity<>(new MenuItemJSONWrapper(item), HttpStatus.OK);
            }
        }
    }

    /**
     * Search item.
     *
     * @param language of the content
     * @param name the menu item
     * @return the menu item
     * @throws ResourceNotFoundException the resource not found exception
     */
    private MenuItem searchMenuItem(final String language, final String name) throws ResourceNotFoundException {
        final MenuItem item = menuItemRepository.findOneByLanguageAndName(language, name);

        if (item == null) {
            throw new ResourceNotFoundException("Menu item " + name + " not found");
        }
        return item;
    }

    /**
     * Search item.
     *
     * @param id the menu item id
     * @return the menu item
     * @throws ResourceNotFoundException the resource not found exception
     */
    private MenuItem searchMenuItem(final Long id) throws ResourceNotFoundException {
        final MenuItem item = menuItemRepository.findOne(id);

        if (item == null) {
            throw new ResourceNotFoundException("Menu item with id=" + id + " not found");
        }
        return item;
    }


    /**
     *
     * @param language
     * @param name
     * @param format
     * @return An empty hidden draft MenuItem
     */
    private MenuItem getEmptyHiddenDraftMenuItem(final String language, final String name, final MenuItem.ContentType format) {
        final List<MenuItem.MenuSection> sections = new ArrayList<>();
        sections.add(MenuItem.MenuSection.DRAFT);
        MenuItem item = new MenuItem(0, language, name, null, null, format, sections, MenuItem.MenuItemStatus.HIDDEN);
        return item;
    }


    /**
     * Set the content with file or with provided link
     *
     * @param data the file
     * @param link the link
     * @param item the menuitem to set content
     */
    private void fillContent(String language, String name, final byte[] data, final String link, final MenuItem item) throws IllegalArgumentException {

        if (language != null && !language.isEmpty()) {
            item.setLanguage(language);
        }

        if (name != null && !name.isEmpty()) {
            item.setName(name);
        }

        if (data != null) {
            item.setData(data);
        }

        if (link != null && !UrlUtils.isValidRedirectUrl(link)) {
            throw new IllegalArgumentException("The link provided is not valid");
        } else {
            item.setLink(link);
        }
    }

    /* HTTP status messages not from ApiParams */

    private static final String MENUITEM_OK = "Menu item found";

    private static final String MENUITEM_NOT_FOUND = "Menu item not found";

    private static final String MENUITEM_DUPLICATE = "Menu item already in the system: use PUT";

    private static final String MENUITEM_SAVED = "Menu item saved";

    private static final String MENUITEM_UPDATED = "Menu item changes saved";

    private static final String MENUITEM_DELETED = "Menu item removed";

    private static final String ERROR_FILE = "File not valid";

    private static final String ERROR_CREATE = "Wrong parameters are provided";

}
