(function() {
    goog.provide('ngr_search_controller');

    goog.require('gn_searchsuggestion_service');
    goog.require('gn_thesaurus_service');
    goog.require('abc');
    goog.require('gn_catalog_service');
    goog.require('search_filter_tags_directive');



    var module = angular.module('ngr_search_controller', [
        'ui.bootstrap.typeahead',
        'gn_searchsuggestion_service',
        'gn_thesaurus_service',
        'gn_catalog_service',
        'search_filter_tags_directive'
    ]);

    /**
     * Main search controller attached to the first element of the
     * included html file from the base-layout.xsl output.
     */
    module.controller('NgrSearchController', [
        '$scope',
        '$q',
        '$http',
        'gnHttp',
        'suggestService',
        'gnAlertService',
        'gnSearchSettings',
        'gnUrlUtils',
        function($scope, $q, $http, gnHttp, suggestService,
                 gnAlertService, gnSearchSettings, gnUrlUtils) {

            /** Object to be shared through directives and controllers */
            $scope.searchObj = {
                params: {},
                permalink: true,
                sortbyValues: gnSearchSettings.sortbyValues,
                sortbyDefault: gnSearchSettings.sortbyDefault,
                hitsperpageValues: gnSearchSettings.hitsperpageValues
            };

            /** Facets configuration */
            $scope.facetsSummaryType = gnSearchSettings.facetsSummaryType;

            /* Pagination configuration */
            $scope.paginationInfo = gnSearchSettings.paginationInfo;

            /* Default result view template */
            $scope.resultTemplate = gnSearchSettings.resultTemplate ||
                gnSearchSettings.resultViewTpls[0].tplUrl;

            $scope.getAnySuggestions = function(val) {
                return suggestService.getAnySuggestions(val);
            };

            $scope.keywordsOptions = {
                mode: 'remote',
                remote: {
                    url: suggestService.getUrl('QUERY', 'keyword', 'STARTSWITHFIRST'),
                    filter: suggestService.bhFilter,
                    wildcard: 'QUERY'
                }
            };

            $scope.orgNameOptions = {
                mode: 'remote',
                remote: {
                    url: suggestService.getUrl('QUERY', 'orgName', 'STARTSWITHFIRST'),
                    filter: suggestService.bhFilter,
                    wildcard: 'QUERY'
                }
            };
              
            $scope.inspirethemeOptions = {
                mode: 'remote',
                remote: {
                   filter: function(data) {
                    var datum = [];
                    data[0].forEach(function(item) {
                      datum.push({
                          id: item.values[0]['#text'],
                          name: item.values[1]['#text']
                        });
                      });
                      return datum;
                    },
                    url: gnUrlUtils.append(gnHttp.getService('keywords'),
                        gnUrlUtils.toKeyValue({
                          pThesauri: 'external.theme.inspire-theme',
                          maxResults: 50,
                          pLang: ['eng','dut'],
                          pKeyword: 'KEYWORD*'
                        })
                    ),
                    wildcard: 'KEYWORD'
                }
            };

            $scope.categoriesOptions = {
                mode: 'prefetch',
                promise: (function() {
                    var defer = $q.defer();
                    $http.get(suggestService.getInfoUrl('categories')).
                    success(function(data) {
                        var mdc = data.metadatacategory|[];
                        var res = [];
                        for (var i = 0; i < mdc.length; i++) {
                            res.push({
                                id: data.metadatacategory[i].name,
                                name: data.metadatacategory[i].label.eng
                            });
                        }
                        defer.resolve(res);
                    });
                    return defer.promise;
                })()
            };

            $scope.sourcesOptions = {
                mode: 'prefetch',
                promise: (function() {
                  var  defer = $q.defer();
                  $http.get(suggestService.getInfoUrl('sources')).
                  success(function(data) {
                    var res = [];
                    var a = data['sources']|[];
                    for (var i = 0; i < a.length; i++) {
                        res.push({
                            id: a[i]['@id'],
                            name: a[i].name
                        });
                    }
                    defer.resolve(res);
                });
                return defer.promise;
            })()
            };

            /**
             * Keep a reference on main cat scope
             * @return {*}
             */
            $scope.getCatScope = function() {return $scope};

            // TODO: see if not redundant with CatController event management
            $scope.$on('StatusUpdated', function(e, status) {
                gnAlertService.addAlert(status);
            });

        }]);
})();
