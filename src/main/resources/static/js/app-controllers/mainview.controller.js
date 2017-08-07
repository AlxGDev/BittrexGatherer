(function () {
    'use strict';

    angular
        .module('app')
        .controller('MainViewController', MainViewController);

    MainViewController.$inject = ['$state', '$scope', '$http', '$filter'];
    function MainViewController($state, $scope, $http, $filter) {
        var vm = this;
        
        vm.orderBook ={
        		buyOrders: {},
        		sellOrders: {}
        };
        vm.buysPropertyName = "id";
        vm.sortBuysBy = sortBuysBy;
        vm.sortSellsBy = sortSellsBy;
        vm.getKeys = getKeys

		
		vm.error = {
				hidden: true,
				message: ""
		};
		
		
		vm.popup1 = { opened: false};
		vm.popup2 = { opened: false};
		
		
		$('.dropdown-menu').find('input').click(function (e) {
		    e.stopPropagation();
		});
		
		init();
		
		function init(){
			setupEventBus();
		}
		
		function setupEventBus() {
			  vm.eb = new EventBus('http://localhost:8080/eventbus');
			  vm.eb.onclose = function (e) {
			    setTimeout(setupEventBus, 1000); // Give the server some time to come back
			  };
			  vm.eb.onopen = function() {

				  // set a handler to receive a message
				  /*vm.eb.registerHandler('UPDATEORDERBOOK:BTC-ARK', function(error, message) {
				    console.log('received a message: ' + JSON.stringify(message));
				  }); */
				  vm.eb.registerHandler('ORDERBOOKREADY:BTC-ARK', function(error, message) {
					    console.log('received a message: ' + JSON.stringify(message));
					    vm.eb.send("GETORDERBOOK:BTC-ARK",
					              "", function(response, json) {
					    				setOrderBook(json.body);
					    			});
				  });
				  
				  vm.eb.send("GETORDERBOOK:BTC-ARK",
			              "", function(response, json) {
					  
		              setOrderBook(json.body);
		              
				  });

			 };
			 vm.eb.onerror = function (error) {
				  console.log("Problem calling event bus " + error)
			 };
			 
		}
		function setOrderBook(msg){
			vm.buysReverse = true;
			var obj = JSON.parse(msg);
			console.log("A");
			vm.orderBook.buyOrders = obj.buyOrders;
			vm.orderBook.sellOrders = obj.sellOrders;
			vm.tradingPair = obj.tradingPair;
			$scope.$apply()
		}
		
		function sortBuysBy(propertyName) {
		    vm.buysReverse = (vm.buysPropertyName === propertyName) ? !vm.buysReverse : false;
		    vm.buysPropertyName = propertyName;
		};
		function sortSellsBy(propertyName) {
		    vm.sellsReverse = (vm.sellsPropertyName === propertyName) ? !vm.sellsReverse : false;
		    vm.sellsPropertyName = propertyName;
		};

		function showAlert(message){
			vm.error.message = message;
			vm.error.hidden = false;
		}
		
		function getKeys(obj){
			  return obj? Object.keys(obj) : [];
		}
		

        

        
       
    }

})();