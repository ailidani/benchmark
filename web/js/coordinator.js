var coordinator = { chart : { data : null } };

//$(Highcharts).setOptions({
//    global: { useUTC: false }
//});

var hchart;

$(document).ready( function() {
    coordinator.chart.data = {
        chart: {
            events: {
                load: function () {
                    hchart = this;
                    // set up the updating of the chart each second
                    // var series = this.series[0];
                    // setInterval(function () {
                    //     var x = (new Date()).getTime(), // current time
                    //     y = Math.round(Math.random() * 100);
                    //     series.addPoint([x, y], true, true);
                    // }, 1000);
                }
            }
        },

        rangeSelector: {
            buttons: [{
                count: 1,
                type: 'minute',
                text: '1M'
            }, {
                count: 5,
                type: 'minute',
                text: '5M'
            }, {
                type: 'all',
                text: 'All'
            }],
            inputEnabled: false,
            selected: 0
        },

        title: {
            text: 'Live Throughput (op/s)'
        },

        exporting: {
            enabled: true
        },

        series: [{
            name: 'Throughput',
            data: (function () {
                var data = [],
                time = (new Date()).getTime(),
                i;

                for (i = -60; i <= 0; i++) {
                    data.push([time + i * 1000, 0]);
                }

                return data;
            }())
        }]
    };

    $('#container').highcharts('StockChart', coordinator.chart.data);

})

// Let the library know where WebSocketMain.swf is:
WEB_SOCKET_FORCE_FLASH = true
// Let the library know where WebSocketMain.swf is:
WEB_SOCKET_SWF_LOCATION = "js/WebSocketMain.swf"

// Write your code in the same way as for native WebSocket:
var ws = new WebSocket('ws://localhost:8887');

ws.onopen = function() {
    console.log('open');
    ws.send('Hello');  // Sends a message.
}

ws.onmessage = function(e) {
    // Receives a message.
    console.log('message', e.data);
    var x = (new Date()).getTime(), // current time
        y = Number(e.data);
        hchart.series[0].addPoint([x, y], true, true);
}

ws.onclose = function() {
    console.log('close');
}

