(function() {

  goog.provide('gn_search_ngr');

  goog.require('cookie_warning');
  goog.require('gn_mdactions_directive');
  goog.require('gn_related_directive');
  goog.require('gn_search');
  goog.require('gn_search_ngr_config');
  goog.require('gn_search_default_directive');
  goog.require('ngr_search_controller');
  goog.require('ngr_multi_location_directive');
  goog.require('ngr_pdok_load_controller');
  goog.require('gn_cors_interceptor');

  var module = angular.module('gn_search_ngr',
      ['gn_search', 'gn_search_ngr_config',
       'gn_search_default_directive', 'gn_related_directive',
       'cookie_warning', 'gn_mdactions_directive', 'ngr_search_controller',
       'ngr_multi_location_directive', 'ngr_search_home_controller',
       'ngr_pdok_load_controller',
       'gn_cors_interceptor']);


  module.filter('escape', function() {
    return window.encodeURIComponent;
  });

  module.controller('gnsSearchPopularController', [
    '$scope', 'gnSearchSettings',
    function($scope, gnSearchSettings) {
      $scope.searchObj = {
        permalink: false,
        params: {
          sortBy: 'popularity',
          from: 1,
          to: 9
        }
      };
    }]);


  module.controller('gnsSearchLatestController', [
    '$scope',
    function($scope) {
      $scope.searchObj = {
        permalink: false,
        params: {
          sortBy: 'changeDate',
          from: 1,
          to: 9
        }
      };
    }]);
  module.controller('gnsNGR', [
    '$scope',
    '$location',
    'suggestService',
    '$http',
    '$translate',
    'gnUtilityService',
    'gnSearchSettings',
    'gnViewerSettings',
    'gnMap',
    'gnMdView',
    'gnMdViewObj',
    'gnWmsQueue',
    'gnSearchLocation',
    'gnOwsContextService',
    'hotkeys',
    'gnGlobalSettings',
    function($scope, $location, suggestService, $http, $translate,
             gnUtilityService, gnSearchSettings, gnViewerSettings,
             gnMap, gnMdView, mdView, gnWmsQueue,
             gnSearchLocation, gnOwsContextService,
             hotkeys, gnGlobalSettings) {

      var viewerMap = gnSearchSettings.viewerMap;
      var searchMap = gnSearchSettings.searchMap;

      $scope.modelOptions = angular.copy(gnGlobalSettings.modelOptions);
      $scope.modelOptionsForm = angular.copy(gnGlobalSettings.modelOptions);
      $scope.gnWmsQueue = gnWmsQueue;
      $scope.$location = $location;
      $scope.activeTab = '/home';
      $scope.currentTabMdView = 'relations';
      $scope.resultTemplate = gnSearchSettings.resultTemplate;
      $scope.facetsSummaryType = gnSearchSettings.facetsSummaryType;
      $scope.location = gnSearchLocation;

      //$scope.relatedRecordsFound = false;

      // $scope.$on('relatedDirectiveRelationsLoaded', function(evt, found) {
      //   console.log("relatedDirectiveRelationsLoaded -> " + found);
      //   if (gnSearchLocation.isMdView()) {
      //     if (found) {
      //       $scope.relatedRecordsFound = true;
      //     }
      //   } else {
      //     $scope.relatedRecordsFound = false;
      //     console.log("relatedDirectiveRelationsLoaded -> Not in MDView" );
      //
      //   }
      // });

      $scope.toggleMap = function () {
        $(searchMap.getTargetElement()).toggle();
      };
      hotkeys.bindTo($scope)
        .add({
            combo: 'h',
            description: $translate.instant('hotkeyHome'),
            callback: function(event) {
              $location.path('/home');
            }
          }).add({
            combo: 't',
            description: $translate.instant('hotkeyFocusToSearch'),
            callback: function(event) {
              event.preventDefault();
              var anyField = $('#gn-any-field');
              if (anyField) {
                gnUtilityService.scrollTo();
                $location.path('/search');
                anyField.focus();
              }
            }
          }).add({
            combo: 'enter',
            description: $translate.instant('hotkeySearchTheCatalog'),
            allowIn: 'INPUT',
            callback: function() {
              $location.search('tab=search');
            }
            //}).add({
            //  combo: 'r',
            //  description: $translate.instant('hotkeyResetSearch'),
            //  allowIn: 'INPUT',
            //  callback: function () {
            //    $scope.resetSearch();
            //  }
          }).add({
            combo: 'm',
            description: $translate.instant('hotkeyMap'),
            callback: function(event) {
              $location.path('/map');
            }
          });


      // TODO: Previous record should be stored on the client side
      $scope.mdView = mdView;
      gnMdView.initMdView();
      $scope.goToSearch = function (any) {
        $location.path('/search').search({'any': any});
      };
      $scope.canEdit = function(record) {
        // TODO: take catalog config for harvested records
        if (record && record['geonet:info'] &&
            record['geonet:info'].edit == 'true') {
          return true;
        }
        return false;
      };
      $scope.openRecord = function(index, md, records) {
        gnMdView.feedMd(index, md, records);
      };

      $scope.closeRecord = function() {
        gnMdView.removeLocationUuid();
        $location.search('tab', null);

      };
      $scope.nextRecord = function() {
        // TODO: When last record of page reached, go to next page...
        $scope.openRecord(mdView.current.index + 1);
      };
      $scope.previousRecord = function() {
        $scope.openRecord(mdView.current.index - 1);
      };

      $scope.infoTabs = {
        lastRecords: {
          title: 'lastRecords',
          titleInfo: '',
          active: true
        },
        preferredRecords: {
          title: 'preferredRecords',
          titleInfo: '',
          active: false
        }};

      // Set the default browse mode for the home page
      $scope.$watch('searchInfo', function () {

        if (angular.isDefined($scope.searchInfo.facet)) {
          if ($scope.searchInfo.facet['inspireThemes'].length > 0) {
            $scope.browse = 'inspire';
          } else if ($scope.searchInfo.facet['topicCats'].length > 0) {
            $scope.browse = 'topics';
          //} else if ($scope.searchInfo.facet['categories'].length > 0) {
          //  $scope.browse = 'cat';
          }
        }

        // change default tab when there are no relations

        // if (!$scope.hasRelations) {
        //   $scope.currentTabMdView = 'general';
        // } else {
        //   $scope.currentTabMdView = 'relations';

        // }

      });

      $scope.resultviewFns = {
        addMdLayerToMap: function (link, md) {

          if (gnMap.isLayerInMap(viewerMap,
              link.name, link.url)) {
            return;
          }
          gnMap.addWmsFromScratch(viewerMap, link.url, link.name, false, md);
      },
        addAllMdLayersToMap: function (layers, md) {
          angular.forEach(layers, function (layer) {
            $scope.resultviewFns.addMdLayerToMap(layer, md);
          });
        },
        loadMap: function (map, md) {
          gnOwsContextService.loadContextFromUrl(map.url, viewerMap);
        }
      };

      // Manage route at start and on $location change
      if (!$location.path()) {
        $location.path('/home');
      }
      $scope.activeTab = $location.path().
          match(/^(\/[a-zA-Z0-9]*)($|\/.*)/)[1];

      var availableTabs = ['general', 'contact', 'relations', 'inspire'];
      $scope.changeTabMdView =function(newTab) {
        if (availableTabs.indexOf(newTab) == -1) {
          newTab = availableTabs[0];
        }
        $location.search('tab', newTab);
      };

      $scope.$on('tabChangeRequested', function(event, requestedTab) {
        //$scope.changeTabMdView(requestedTab);
        $scope.changeTabWithoutModifyingUrl(requestedTab);
      });

      $scope.changeTabWithoutModifyingUrl = function (newTab) {
        if (newTab && availableTabs.indexOf(newTab) != -1) {
          $scope.currentTabMdView = newTab;
        } else {
          $scope.currentTabMdView = 'relations';
        }
      };

      $scope.$on('$locationChangeSuccess', function(next, current) {
        $scope.activeTab = $location.path().
            match(/^(\/[a-zA-Z0-9]*)($|\/.*)/)[1];

        var search = $location.search();

        if (search.tab && availableTabs.indexOf(search.tab) != -1) {
          $scope.currentTabMdView = search.tab;
        } else {
          $scope.currentTabMdView = 'relations';
        }

        if (gnSearchLocation.isSearch() && (!angular.isArray(
            searchMap.getSize()) || searchMap.getSize()[0] < 0)) {
          setTimeout(function() {
            searchMap.updateSize();



            // TODO: load custom context to the search map
            //gnOwsContextService.loadContextFromUrl(
            //  gnViewerSettings.defaultContext,
            //  searchMap);

          }, 0);
        }
        if (gnSearchLocation.isMap() && (!angular.isArray(
            viewerMap.getSize()) || viewerMap.getSize().indexOf(0) >= 0)) {
          setTimeout(function() {
            viewerMap.updateSize();



          }, 0);
        }
      });


      // $scope.$on('hasRelations', function(event, data) {

      //   $scope.hasRelations = data;

      //   if (!data) {
      //     $scope.currentTabMdView = 'general';
      //   } else {
      //     $scope.currentTabMdView = 'relations';
      //   }
      // });

      angular.extend($scope.searchObj, {
        advancedMode: false,
        from: 1,
        to: 30,
        viewerMap: viewerMap,
        searchMap: searchMap,
        mapfieldOption: {
          relations: ['within']
        },
        defaultParams: {
          'facet.q': '',
          resultType: gnSearchSettings.facetsSummaryType || 'details'
        },
        params: {
          'facet.q': '',
          resultType: gnSearchSettings.facetsSummaryType || 'details'
        }
      }, gnSearchSettings.sortbyDefault);


    }]);
})();
