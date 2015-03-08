var dateSocket = new WebSocket("ws://localhost:9000/ws/quotes")

    var receiveEvent = function(event) {
        console.log(event)
    }


    dateSocket.onmessage = receiveEvent