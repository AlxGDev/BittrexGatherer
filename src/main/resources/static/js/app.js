(function () {
    'use strict';

    angular
        .module('app', ['ui.router','ui.bootstrap', 'chart.js'])
        .config(config)
        .run(run);
   
   config.$inject = ['$stateProvider', '$urlRouterProvider', 'ChartJsProvider'];
   function config($stateProvider, $urlRouterProvider, ChartJsProvider) {
	   ChartJsProvider.setOptions({ colors : [ '#EA3925', '#1CA539', '#DCDCDC', '#46BFBD', '#FDB45C', '#949FB1', '#4D5360'] });
	   
	   $urlRouterProvider.otherwise('/');
	   $stateProvider
       .state('main', {
    	   		url: '/',
                controller: 'MainViewController',
                templateUrl: '/views/main.view.html',
                controllerAs: 'vm',
                module: 'public'
       })
	   
	   
   }

    run.$inject = ['$rootScope', '$state', '$http'];
    function run($rootScope, $state, $http, CoinService) {
    	
        
    }

})();