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
				  vm.eb.registerHandler('UPDATEORDERBOOK:BTC-ARK', function(error, message) {
				    console.log('received a message: ' + JSON.stringify(message));
				    processOrderBookUpdate(message.body)
				  }); 
				  vm.eb.registerHandler('ORDERBOOKREADY:BTC-ARK', function(error, message) {
					    console.log('received a message: ' + JSON.stringify(message));
					    vm.eb.send("GETORDERBOOK:BTC-ARK",
					              "", function(response, json) {
					    	 //console.log('received a message: ' + JSON.stringify(json));
					    				setOrderBook(json.body);
					    				
					    			});
				  });
				  
				  vm.eb.send("GETORDERBOOK:BTC-ARK",
			              "", function(response, json) {
					  //console.log('received a message: ' + JSON.stringify(json));
		              setOrderBook(json.body);
		              
		              
				  });

			 };
			 vm.eb.onerror = function (error) {
				  console.log("Problem calling event bus " + error)
			 };
			 
		}
		function setOrderBook(body){
			vm.buysReverse = true;
			
			vm.orderBook.buyOrders = body.buyOrders;
			vm.orderBook.sellOrders = body.sellOrders;
			vm.tradingPair = body.tradingPair;
			$scope.$apply()
		}
		
		function processOrderBookUpdate(body){
			for(var i = 0; i < body.Buys.length; i++){
				
				var key = body.Buys[i].Rate.toFixed(8).toString().replace(".",",");
			   
			    if(body.Buys[i].Quantity == 0){
			    	delete vm.orderBook.buyOrders[key];
			    } else {
			    	 vm.orderBook.buyOrders[key] = body.Buys[i].Quantity;
			    }
			    
			}
			for(var j = 0; j < body.Sells.length; j++){
				
				var key = body.Sells[j].Rate.toFixed(8).toString().replace(".",",");
			   
			    if(body.Sells[j].Quantity == 0){
			    	delete vm.orderBook.sellOrders[key];
			    } else {
			    	 vm.orderBook.sellOrders[key] = body.Sells[j].Quantity;
			    }
			    
			}
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