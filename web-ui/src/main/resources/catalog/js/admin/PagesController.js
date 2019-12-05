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
    goog.provide('gn_pages_controller');
  
  
    var module = angular.module('gn_pages_controller',
        []);
  
  
    /**
     * GnpageController provides management interface
     * for page configuration used for geopublication.
     *
     */
    module.controller('GnPagesController', [
      '$scope', '$http', '$rootScope', '$translate',
      function($scope, $http, $rootScope, $translate) {
  
        $scope.pages = {};
        $scope.pageselected = null;
        $scope.pageUpdated = false;
        $scope.pagesearch = '';
        $scope.isUpdate = null;
        $scope.langSelected = $scope.langLabels[0];
        $scope.menuSelected = "MENU";
        $scope.loadpages = function () {
          $scope.pageselected = null;
          $http.get('../api/pages/list?language='+$scope.langSelected)
              .success(function(data) {
                if (!angular.isArray(data)) data=[];
                $scope.pages = data;
              });
        }
  
        $scope.updatingpage = function() {
          $scope.pageUpdated = true;
        };
  
        $scope.selectpage = function(v) {
          $scope.isUpdate = true;
          $scope.pageUpdated = false;
          $scope.pageselected = v;
        };
  
        $scope.addpage = function() {
          $scope.isUpdate = false;
          $scope.pageselected = {
            'data': '',
            'language': '',
            'linkText':'',
            'link': '',
            'format': '',
            'publish':true
          };
        };
        $scope.savepage = function() {
          $http.put('../api/pages' +
              ($scope.isUpdate ? '/' +
              $scope.pageselected.linkText : ''),
              $scope.pageselected)
              .success(function(data) {
                loadpages();
                $rootScope.$broadcast('StatusUpdated', {
                  msg: $translate.instant('pageUpdated'),
                  timeout: 2,
                  type: 'success'});
              })
              .error(function(data) {
                $rootScope.$broadcast('StatusUpdated', {
                  title: $translate.instant('pageUpdateError'),
                  error: data,
                  timeout: 0,
                  type: 'danger'});
              });
        };
  
        $scope.deletepage = function() {
          $http.delete('../api/pages/' +
              $scope.pageselected.linkText)
              .success(function(data) {
                loadpages();
              })
              .error(function(data) {
                $rootScope.$broadcast('StatusUpdated', {
                  title: $translate.instant('pageDeleteError'),
                  error: data,
                  timeout: 0,
                  type: 'danger'});
              });
        };
        $scope.loadpages();
      }]);
  })();
  