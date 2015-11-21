angular
	.module('app', [ 'ngResource', 'ui.bootstrap', 'smart-table', 'ui.select',
	                 'ui.bootstrap.showErrors' ])
	.directive('requireMultiple', function () {
		return {
			require: 'ngModel',
			link: function postLink(scope, element, attrs, ngModel) {
				ngModel.$validators.required = function (value) {
					return angular.isArray(value) && value.length > 0;
				};
			}
		};
	})
	.directive('mustMatch', ['$parse', function ($parse) {
		return {
			require: '?ngModel',
			restrict: 'A',
			link: function(scope, elem, attrs, ctrl) {
				// if ngModel is not defined, we don't need to do anything
				if (!ctrl) return;
				if (!attrs['mustMatch']) return;
				var firstPassword = $parse(attrs['mustMatch']);
				var validator = function (value) {
					var temp = firstPassword(scope),
						v = value === temp;
					ctrl.$setValidity('match', v);
					return value;
				}
				ctrl.$parsers.unshift(validator);
				ctrl.$formatters.push(validator);
				attrs.$observe('mustMatch', function () {
					validator(ctrl.$viewValue);
				});
			}
		};
	}])
	.filter('toRoleName', function() {
		return function(input) {
			input = input || [];
			var out = '';
			for (var i = 0; i < input.length; i++) {
				if (i > 0) {
					out += ', ';
				}
				var r = input[i];
				if (r === 'admin') {
					out += 'Администратор';
				} else if (r === 'user') {
					out += 'Пользователь';
				} else {
					out += '???';
				}
			}
			return out;
		};
	})
	.config(['showErrorsConfigProvider', function(showErrorsConfigProvider) {
		showErrorsConfigProvider.showSuccess(true);
	} ])
	.factory('alertService', ['$timeout', function($timeout) {
		var ALERT_TIMEOUT = 10000;
		function add(type, msg, timeout) {
			if (timeout) {
				$timeout(function(){
					closeAlert(this);
				}, timeout);
			} else {
				$timeout(function(){
					closeAlert(this);
				}, ALERT_TIMEOUT);
			}

			return alerts.push({
				type: type,
				msg: msg,
				close: function() {
					return closeAlert(this);
				}
			});
		}

		function closeAlert(alert) {
			return closeAlertIdx(alerts.indexOf(alert));
		}

		function closeAlertIdx(index) {
			return alerts.splice(index, 1);
		}

		function clear(){
			alerts = [];
		}

		function get() {
			return alerts;
		}

		var service = {
			add: add,
			closeAlert: closeAlert,
			closeAlertIdx: closeAlertIdx,
			clear: clear,
			get: get
		},
		alerts = [];

		return service;
	} ])
	.controller('AlertsCtrl', ['$scope', 'alertService', function($scope, alertService) {
		$scope.alerts = alertService.get();
	} ])
	.factory('User', [ '$resource',	function($resource) {
		return $resource('', {}, {
			query : {
				method : 'GET',
				isArray : true,
				url : jsRoutes.controllers.UserController.getUsers().url },
			get : {
				method : 'GET',
				url : jsRoutes.controllers.UserController.getUser(':userId').url },
			create : {
				method : 'POST',
				url : jsRoutes.controllers.UserController.createUser().url },
			update : {
				method : 'PUT',
				url : jsRoutes.controllers.UserController.updateUser(':userId').url },
			remove : {
				method : 'DELETE',
				url : jsRoutes.controllers.UserController.removeUser(':userId').url } });
	} ])
	.factory('Password', [ '$resource',	function($resource) {
		return $resource('', {}, {
			update : {
				method : 'PUT',
				url : jsRoutes.controllers.UserController.changeUserPassword(':userId').url } });
	} ])
	.controller('EditModalInstanceCtrl',	[ '$scope', '$uibModalInstance', 'user', 'roles', 'alertService',
	    function($scope, $uibModalInstance, user, roles, alertService) {
			$scope.title = 'Редактирование пользователя ' + user.name;
			$scope.user = user;
			$scope.roles = roles;
	
			$scope.save = function() {
				$scope.$broadcast('show-errors-check-validity');
				if ($scope.mainForm.$valid) {
					$scope.user.$update({userId: user.id}, function() {
						// success
						$uibModalInstance.close($scope.user);
					}, function(data) {
						if(data.status === 400) {
							angular.forEach(data.data, function(value, key) {
								if(key == "error") {
									alertService.add('danger', value.message);
								} else {
									$scope.mainForm[key].$error.custom = Messages(value[0]);
									$scope.mainForm[key].$invalid = true;
									$scope.mainForm[key].$valid = false;
								}
							});
							$scope.$broadcast('show-errors-check-validity');
						}
						if(data.status === 500) {
							alertService.add('danger', Messages('error.InternalServerError'));
						}
					});
				}
			};
			$scope.cancel = function() {
				$uibModalInstance.dismiss('cancel');
			};
		}
	])
	.controller('AddModalInstanceCtrl',	[ '$scope', '$uibModalInstance', 'User', 'roles', 'alertService',
	    function($scope, $uibModalInstance, User, roles, alertService) {
			$scope.title = 'Создание пользователя';
			$scope.user = {};
			$scope.roles = roles;
	
			$scope.save = function() {
				$scope.$broadcast('show-errors-check-validity');
				if ($scope.mainForm.$valid) {
					User.create($scope.user, function(user) {
						// success
						$uibModalInstance.close(user);
					}, function(data) {
						if(data.status === 400) {
							angular.forEach(data.data, function(value, key) {
								if(key == "error") {
									alertService.add('danger', value.message);
								} else {
									$scope.mainForm[key].$error.custom = Messages(value[0]);
									$scope.mainForm[key].$invalid = true;
									$scope.mainForm[key].$valid = false;
								}
							});
							$scope.$broadcast('show-errors-check-validity');
						}
						if(data.status === 500) {
							alertService.add('danger', Messages('error.InternalServerError'));
						}
					});
				}
			};
			$scope.cancel = function() {
				$uibModalInstance.dismiss('cancel');
			};
		}
	])
	.controller('ChangePasswordModalInstanceCtrl',	[ '$scope', '$uibModalInstance', 'userId', 'Password',
	                                       	  'alertService',
	    function($scope, $uibModalInstance, userId, Password, alertService) {
			$scope.pwdObj = {};
	
			$scope.save = function() {
				$scope.$broadcast('show-errors-check-validity');
				if ($scope.mainForm.$valid) {
					Password.update({userId: userId}, $scope.pwdObj, function() {
						// success
						$uibModalInstance.close();
					}, function(data) {
						if(data.status === 400) {
							angular.forEach(data.data, function(value, key) {
								if(key == "error") {
									alertService.add('danger', value.message);
								} else {
									$scope.mainForm[key].$error.custom = Messages(value[0]);
									$scope.mainForm[key].$invalid = true;
									$scope.mainForm[key].$valid = false;
								}
							});
							$scope.$broadcast('show-errors-check-validity');
						}
						if(data.status === 500) {
							alertService.add('danger', Messages('error.InternalServerError'));
						}
					});
				}
			};
			$scope.cancel = function() {
				$uibModalInstance.dismiss('cancel');
			};
		}
	])
	.controller('RemoveUserCtl', [ '$scope', '$uibModalInstance', 'User', 'user', 'alertService',
		function($scope, $uibModalInstance, User, user, alertService) {
			$scope.user = user;
			$scope.removeUser = function() {
				User.remove({userId: user.id}, function(){
					// success
					$uibModalInstance.close();
				}, function(data){
					// fail
					if(data.status === 500) {
						alertService.add('danger', Messages('error.InternalServerError'));
					} else if (typeof data.data != 'undefined' && typeof data.data.error != 'undefined'){
						alertService.add('danger', data.data.error.message);
					} else {
						alertService.add('danger', Message('error.unknown'));
					}
				});
			};
			$scope.cancel = function() {
				$uibModalInstance.dismiss('cancel');
			};
		}
	])
	.controller('UsersCtl',	['$scope', 'User', '$uibModal', '$log', '$filter',
	    function($scope, User, uibModal, $log, $filter) {
			$scope.users = User.query();
			$scope.roles = [
				{id: "admin", label: Messages("label.roles.admin")},
				{id: "user", label: Messages("label.roles.user")}
			]; //{@Html(models.user.RoleName.asList().map(a => "\"" + a + "\" : \"" + Messages("label.roles." + a) + "\"").mkString(","))};
			$scope.editUser = function(userId) {
				$scope.user = User.get(
					{ userId : userId },
					function() {
						var modalInstance = uibModal.open({
							animation : true,
							templateUrl : 'addEditDialog',
							controller : 'EditModalInstanceCtrl',
							resolve : {
								user : function() {
									return $scope.user;
								},
								roles : function() {
									return $scope.roles;
								}
							}
						});

						modalInstance.result.then(function(user) {
								var oldUser = $filter('filter')($scope.users, {id: user.id}, true)[0];
								angular.extend(oldUser, user);
							}, function() {
								$log.info('Modal dismissed at: ' + new Date());
							}
						);
					}
				);
			};
			$scope.createUser = function() {
				$scope.user = {};
				var modalInstance = uibModal.open({
					animation : true,
					templateUrl : 'addEditDialog',
					controller : 'AddModalInstanceCtrl',
					resolve : {
						roles : function() {
							return $scope.roles;
						}
					}
				});

				modalInstance.result.then(function(user) {
						$scope.users.push(user);
					}, function() {
						$log.info('Modal dismissed at: ' + new Date());
					}
				);
			};
			$scope.changePassword = function(userId) {
				var modalInstance = uibModal.open({
					animation : true,
					templateUrl : 'changePasswordDialog',
					controller : 'ChangePasswordModalInstanceCtrl',
					resolve : {
						userId : function() {
							return userId;
						}
					}
				});

				modalInstance.result.then(function() {}, function() {
					$log.info('Modal dismissed at: ' + new Date());
				});
			}
			$scope.removeUser = function(user, $index) {
				var modalInstance = uibModal.open({
					animation : true,
					templateUrl : 'confirmUserRemoveDialog',
					controller : 'RemoveUserCtl',
					resolve : {
						user : function() {
							return user;
						}
					}
				});

				modalInstance.result.then(function() {
					$scope.users.splice($index, 1);
				}, function() {
					$log.info('Modal dismissed at: ' + new Date());
				});
				// 2. send DELETE request to server
				// 3. remove line from table if request was success
			}
		}
	]);
