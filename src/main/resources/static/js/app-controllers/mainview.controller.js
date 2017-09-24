(function () {
    'use strict';

    angular
        .module('app')
        .controller('MainViewController', MainViewController);

    MainViewController.$inject = ['$state', '$scope', '$http', '$filter', 'BittrexService'];
    function MainViewController($state, $scope, $http, $filter, BittrexService) {
        var vm = this;
        
        vm.orderBook ={
        		buyOrders: {},
        		sellOrders: {}
        };
        vm.tradingpairs = [];
        vm.indicators = {};
        vm.currentTradingPair = "BTC-ARK";
        vm.buysPropertyName = "id";
        vm.sortBuysBy = sortBuysBy;
        vm.sortSellsBy = sortSellsBy;
        vm.getKeys = getKeys;
        vm.switchTo = switchTo;

		
		vm.error = {
				hidden: true,
				message: ""
		};
		
		
		vm.popup1 = { opened: false};
		vm.popup2 = { opened: false};
		
		var orderBookUpdateCallBack = function(error, message) {
		    //console.log('received a message: ' + JSON.stringify(message));
		    processOrderBookUpdate(message.body)
		};
		
		var orderBookReadyCallBack = function(error, message) {
		    //console.log('received a message: ' + JSON.stringify(message));
		    vm.eb.send("GETORDERBOOK:"+vm.currentTradingPair,
		              "", function(response, json) {
		    	 //console.log('received a message: ' + JSON.stringify(json));
		    				setOrderBook(json.body);
		    				
		    			});
		};
		
		var indicatorsUpdateCallBack = function(error, message) {
		    console.log('received a message: ' + JSON.stringify(message));
		    vm.indicators = message.body;
		};
		
		
		init();
		
		function init(){
			
			BittrexService.getTradingPairs(function (result) {
            	if(result.success == true){
            		vm.tradingpairs = result.message;
            		vm.error.hidden = true;
            		setupEventBus();
            		
            	} else {
            		showAlert(result.message);
            	}
                
            });
			
			
		}
		
		function switchTo(tradingpair){
			console.log("Switching");
			if(vm.eb == null || vm.eb.state != EventBus.OPEN){
				showAlert("EventBus not open!");
			} else {
				 vm.eb.unregisterHandler('UPDATEORDERBOOK:'+vm.currentTradingPair, orderBookUpdateCallBack); 
				 vm.eb.unregisterHandler('ORDERBOOKREADY:'+vm.currentTradingPair, orderBookReadyCallBack);
				 vm.eb.unregisterHandler('UPDATEINDICATORS:'+vm.currentTradingPair, indicatorsUpdateCallBack);
				 vm.currentTradingPair = tradingpair;
				 vm.orderBook ={
			        		buyOrders: {},
			        		sellOrders: {}
			     };
				 vm.indicators ={};
				 vm.eb.registerHandler('UPDATEORDERBOOK:'+vm.currentTradingPair, orderBookUpdateCallBack); 
				 vm.eb.registerHandler('ORDERBOOKREADY:'+vm.currentTradingPair, orderBookReadyCallBack);
				 vm.eb.registerHandler('UPDATEINDICATORS:'+vm.currentTradingPair, indicatorsUpdateCallBack);
				 vm.eb.send("REDEPLOYBITTREXVERTICLES",
						 vm.currentTradingPair, function(response, json) {
					  console.log('reply from redeploy: ' + response);
		              
		              
				  });
				 
			}
		}
		
		function setupEventBus() {
			  vm.eb = new EventBus('http://localhost:8080/eventbus');
			  vm.eb.onclose = function (e) {
			    setTimeout(setupEventBus, 1000); // Give the server some time to come back
			  };
			  vm.eb.onopen = function() {
				  vm.error.hidden = true;
				  // set a handler to receive a message
				  vm.eb.registerHandler('UPDATEORDERBOOK:'+vm.currentTradingPair, orderBookUpdateCallBack); 
				  vm.eb.registerHandler('ORDERBOOKREADY:'+vm.currentTradingPair, orderBookReadyCallBack);
				  vm.eb.registerHandler('UPDATEINDICATORS:'+vm.currentTradingPair, indicatorsUpdateCallBack);
				  vm.eb.send("GETORDERBOOK:"+vm.currentTradingPair,
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
			 vm.currentTradingPair = body.tradingPair;
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