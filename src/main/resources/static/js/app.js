

(function () {
    'use strict';

    angular
        .module('app', ['ui.router','ui.bootstrap'])
        .config(config)
        .run(run);
   
   config.$inject = ['$stateProvider', '$urlRouterProvider'];
   function config($stateProvider, $urlRouterProvider) {
	   
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