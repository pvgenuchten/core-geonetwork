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
  goog.provide('gn_editor');

  goog.require('gn_batch_service');
  goog.require('gn_draggable_directive');
  goog.require('gn_editor_controller');
  goog.require('gn_geopublisher');
  goog.require('gn_mdactions_directive');
  goog.require('gn_module');
  goog.require('gn_onlinesrc');
  goog.require('gn_ows');
  goog.require('gn_popup');
  goog.require('gn_suggestion');
  goog.require('gn_validation');

  var module = angular.module('gn_editor', [
    'gn_module',
    'gn_popup',
    'gn_onlinesrc',
    'gn_suggestion',
    'gn_validation',
    'gn_draggable_directive',
    'gn_editor_controller',
    'gn_batchedit_controller',
    'gn_ows',
    'gn_geopublisher',
    'gn_batch_service',
    'gn_mdactions_directive',
    'ui.ace'
  ]);

  module.constant('gnViewerSettings', {});

  module.config(['$LOCALES','gnGlobalSettings', 
    function($LOCALES) {
      $LOCALES.push('search');
      $LOCALES.push('editor');
      //searchView is defined on base-layout-cssjs-loader.xsl
      $LOCALES.push('/../../catalog/views/'+searchView+
          '/locales/'+gnGlobalSettings.lang+'-core.json');
      $LOCALES.push('/../api/0.1/tools/i18n/db?' +
          'type=StatusValue&type=Operation&type=Group');

    }]);
})();
