(function(){
    goog.provide('ngr_pdok_load_controller');

    goog.require('gn_cat_controller');

    var module = angular.module('ngr_pdok_load_controller', ['gn_cat_controller']);

    module.controller('ngrPdokLoad', 
        [
          '$http',
          'gnLangs',
          '$scope',
          '$attrs',
          '$element',
        function($http, gnLangs, $scope, $attrs, $element) {
            var lang = "nl";
            if(gnLangs.getIso2Lang() == "en") {
              lang = gnLangs.getIso2Lang();
            }
            
            $http.get("https://www.pdok.nl/" + lang + "/ngr.xml").
              success(function(data, status) {
                var xml = $.parseXML(data);
                $.each(xml.getElementsByTagName("item"), function(i, item) {
                  var tmp = item.getElementsByTagName("link")[0];
                  var link = tmp.innerText || tmp.textContent;
                  if(link == $attrs.item) {
                    var tmp = item.getElementsByTagName("title")[0];
                    $scope.title = tmp.innerText || tmp.textContent;
                    $scope.link = link;
                    var tmp = item.getElementsByTagName("description")[0];
                    $scope.description = tmp.innerText || tmp.textContent;
                    $($element.find(".content")).html($scope.description);
                   }
                  }
                );
              });
      }  
    ]);

}());