(function () {
    'use strict';

    angular
        .module('app')
        .factory('BittrexService', BittrexService);

   BittrexService.$inject = ['$http'];
    function BittrexService($http) {
        var service = {};
        
        
        service.getTradingPairs = getTradingPairs;
        

        return service;

        
        function getTradingPairs(callback) {
        	
        	var url = "/api/tradingpairs";
        	
        	$http.get(url).then(function successCallback(response) {
					callback({ success: true, message: response.data.result});
				}, function errorCallback(response) {
					console.log("Error: "+response);
					callback({ success: false, message:'Error loading markets'});
					  
				});
        	
		}
        
        

        
    }

})();
