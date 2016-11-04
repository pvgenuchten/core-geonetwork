(function() {

  goog.provide('gn_search_ngr_config');

  goog.require('ol.proj.EPSG28992');

  var module = angular.module('gn_search_ngr_config', ['ol.proj.EPSG28992']);

  module.value('gnTplResultlistLinksbtn',
        '../../catalog/views/default/directives/partials/linksbtn.html');

  module
      .run([
        'gnSearchSettings',
        'gnViewerSettings',
        'gnOwsContextService',
        'gnMap',
        'gnNcWms',
        'gnConfig',
        function(searchSettings, viewerSettings, gnOwsContextService,
                 gnMap, gnNcWms, gnConfig) {
          // Load the context defined in the configuration
          /*viewerSettings.defaultContext =
            viewerSettings.mapConfig.viewerMap ||
            '../../map/config-viewer.xml';*/
          viewerSettings.defaultContext = null;

          // Keep one layer in the background
          // while the context is not yet loaded.
          viewerSettings.bgLayers = [
            gnMap.createLayerForType('osm')
          ];

          viewerSettings.bingKey = 'AnElW2Zqi4fI-9cYx1LHiQfokQ9GrNzcjOh_' +
              'p_0hkO1yo78ba8zTLARcLBIf8H6D';

          viewerSettings.servicesUrl =
            viewerSettings.mapConfig.listOfServices || {};

          // WMS settings
          // If 3D mode is activated, single tile WMS mode is
          // not supported by ol3cesium, so force tiling.
          if (gnConfig['map.is3DModeAllowed']) {
            viewerSettings.singleTileWMS = false;
            // Configure Cesium to use a proxy. This is required when
            // WMS does not have CORS headers. BTW, proxy will slow
            // down rendering.
            viewerSettings.cesiumProxy = true;
          } else {
            viewerSettings.singleTileWMS = true;
          }

          var bboxStyle = new ol.style.Style({
            stroke: new ol.style.Stroke({
              color: 'rgba(255,0,0,1)',
              width: 2
            }),
            fill: new ol.style.Fill({
              color: 'rgba(255,0,0,0.3)'
            })
          });
          searchSettings.olStyles = {
            drawBbox: bboxStyle,
            mdExtent: new ol.style.Style({
              stroke: new ol.style.Stroke({
                color: 'orange',
                width: 2
              })
            }),
            mdExtentHighlight: new ol.style.Style({
              stroke: new ol.style.Stroke({
                color: 'orange',
                width: 3
              }),
              fill: new ol.style.Fill({
                color: 'rgba(255,255,0,0.3)'
              })
            })

          };

          // Object to store the current Map context
          viewerSettings.storage = 'sessionStorage';

          /*******************************************************************
           * Define maps
           */
          var matrixIds=[];
			    var matrixIds2=[];

          for (var i=0;i<=12;++i) {
            if (i<10){
  			      matrixIds[i]="0"+i;
  			      matrixIds2[i]="EPSG:28992:"+i;
  			    } else {
              matrixIds[i]=""+i;
  			      matrixIds2[i]="EPSG:28992:"+i;
  			    }
          }

          var resolutions = [3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84];
			    var tileLayers = [
            new ol.layer.Tile({
              title:'BRT',attribution:'PDOK',
              source: new ol.source.WMTS({
                url: '//geodata.nationaalgeoregister.nl/tiles/service/wmts/brtachtergrondkaartgrijs',
                layer: 'brtachtergrondkaartgrijs',
                matrixSet: 'EPSG:28992',
                format: 'image/png',
                projection: ol.proj.get('EPSG:28992'),
                tileGrid: new ol.tilegrid.WMTS({
                  origin: [-285401.92,903402.0],
                  resolutions: resolutions,
                  matrixIds: matrixIds2
                }),
                wrapX: true
              })
            }),
            new ol.layer.Tile({
              title:'Luchtfoto',attribution:'PDOK',visible:false,
              source: new ol.source.WMTS({
                url: '//geodata1.nationaalgeoregister.nl/luchtfoto/wmts?style=default&',
                layer: 'luchtfoto',
                matrixSet: 'nltilingschema',
                format: 'image/jpeg',
                projection: ol.proj.get('EPSG:28992'),
                tileGrid: new ol.tilegrid.WMTS({
                  origin: [-285401.92,903402.0],
                  resolutions: resolutions,
                  matrixIds: matrixIds,
                  style:'default'
                }),
                wrapX: true
              })
            })
          ];

    			//important to set the projection info here (also), used as view configuration
    			var mapsConfig = {
      			resolutions: resolutions,
      			extent: [-285401.92,22598.08,595401.92,903401.92],
      			projection: 'EPSG:28992',
      			center: [150000, 450000],
      			zoom: 3
    			};

    			// Add backgrounds to TOC
    			viewerSettings.bgLayers = tileLayers;
    			viewerSettings.servicesUrl = {};

    			//Configure the ViewerMap
    			var viewerMap = new ol.Map({
      			controls:[],
      			layers: tileLayers,
      			view: new ol.View(mapsConfig)
    			});

    			//configure the SearchMap
    			var searchMap = new ol.Map({
      			controls:[],
      			layers: [tileLayers[0]],
      			view: new ol.View(mapsConfig)
    			});

          /** Facets configuration */
          searchSettings.facetsSummaryType = 'details';

          /*
           * Hits per page combo values configuration. The first one is the
           * default.
           */
          searchSettings.hitsperpageValues = [20, 50, 100];

          /* Pagination configuration */
          searchSettings.paginationInfo = {
            hitsPerPage: searchSettings.hitsperpageValues[0]
          };

          /*
           * Sort by combo values configuration. The first one is the default.
           */
          searchSettings.sortbyValues = [{
            sortBy: 'relevance',
            sortOrder: ''
          }, {
            sortBy: 'changeDate',
            sortOrder: ''
          }, {
            sortBy: 'title',
            sortOrder: 'reverse'
          }, {
            sortBy: 'rating',
            sortOrder: ''
          }, {
            sortBy: 'popularity',
            sortOrder: ''
          }, {
            sortBy: 'denominatorDesc',
            sortOrder: ''
          }, {
            sortBy: 'denominatorAsc',
            sortOrder: 'reverse'
          }];

          /* Default search by option */
          searchSettings.sortbyDefault = searchSettings.sortbyValues[0];

          /* Custom templates for search result views */
          searchSettings.resultViewTpls = [{
            tplUrl: '../../catalog/views/ngr/templates/card.html',
            tooltip: 'Grid',
            icon: 'fa-th'
          }];

          // For the time being metadata rendering is done
          // using Angular template. Formatter could be used
          // to render other layout

          // TODO: formatter should be defined per schema
          // schema: {
          // iso19139: 'md.format.xml?xsl=full_view&&id='
          // }
          searchSettings.formatter = {
            // defaultUrl: 'md.format.xml?xsl=full_view&id='
            // defaultUrl: 'md.format.xml?xsl=xsl-view&uuid=',
            // defaultPdfUrl: 'md.format.pdf?xsl=full_view&uuid=',
            list: [{
              label: 'full',
              url : function(md) {
                return '../api/records/' + md.getUuid() + '/formatters/xsl-view?root=div&view=advanced';
              }
            }]
          };

          // Mapping for md links in search result list.
          searchSettings.linkTypes = {
            links: ['LINK', 'kml'],
            downloads: ['DOWNLOAD'],
            //layers:['OGC', 'kml'],
            layers:['OGC'],
            maps: ['ows']
          };

          // Set the default template to use
          searchSettings.resultTemplate =
              searchSettings.resultViewTpls[0].tplUrl;

          // Set custom config in gnSearchSettings
          angular.extend(searchSettings, {
            viewerMap: viewerMap,
            searchMap: searchMap
          });

          viewerMap.getLayers().on('add', function(e) {
            var layer = e.element;
            if (layer.get('advanced')) {
              gnNcWms.feedOlLayer(layer);
            }
          });

        }]);
})();
