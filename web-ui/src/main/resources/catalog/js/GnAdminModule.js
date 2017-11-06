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

(function() {
  goog.provide('gn_admin');










  goog.require('gn_admin_controller');
  goog.require('gn_module');

  var module = angular.module('gn_admin', [
    'gn_module',
    'gn_admin_controller'
  ]);


  module.config(['$LOCALES','gnGlobalSettings', 
    function($LOCALES) {
      $LOCALES.push('admin');
      //searchView is defined on base-layout-cssjs-loader.xsl
      $LOCALES.push('/../../catalog/views/'+searchView+
          '/locales/'+gnGlobalSettings.lang+'-core.json')};
    }]);
})();
