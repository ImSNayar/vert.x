'use strict';

angular.module("wikiApp", [])
    .controller("WikiController", ["$scope", "$http", "$timeout", function ($scope, $http, $timeout) {

        var eb = new EventBus("http://localhost:9091/eventbus/");
        console.log(eb);
        $scope.divContent = "";

        $scope.sendMessage = function () {
            console.log("Sending message: " + eb.state);
            if (eb.state !== EventBus.OPEN) return;

            eb.send("service_a", "request_body_html", function (err, reply) {
                if (err === null) {
                    console.log(reply);
                    $scope.divContent = reply.body;
                } else {
                    console.warn("Error rendering : " + JSON.stringify(err));
                }
            });
            // end::eventbus-markdown-sender[]
        }


    }]);
