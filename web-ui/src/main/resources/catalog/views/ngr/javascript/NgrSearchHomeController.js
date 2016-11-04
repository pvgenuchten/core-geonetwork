(function () {
    goog.provide('abc');

    var module = angular.module('ngr_search_home_controller', []);

    module.controller('NgrSearchHomeController', ['$scope', '$location', '$log',
        function ($scope, $location, $log) {

          $scope.searchHomeParams = {
            anyParameter: null,
			geometry: null
          };

        $scope.performSearchHome = function() {

            var any = $scope.searchHomeParams.anyParameter;
            var geometry = $scope.searchHomeParams.geometry;

            var searchParams = {
              any: any
            };
            if (geometry) {
                angular.extend(searchParams, {geometry: geometry});
            }

            $location.path('/search').search(searchParams);

        };


        $scope.$on('$locationChangeSuccess', function(event, newUrl) {
            var activeTab = $location.path().
                match(/^(\/[a-zA-Z0-9]*)($|\/.*)/)[1];
            // reset search paramameters
            if (activeTab === '/home') {
                $scope.searchHomeParams = {
					anyParameter: null,
                    geometry: null
                };
            }
        });

    }]);

})();
