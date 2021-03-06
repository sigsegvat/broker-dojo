



$(document).ready(function() {

    var Tick = Backbone.Model.extend({
                           idAttribute: "nr"
                 });

    var Ticks = Backbone.Collection.extend({
         model: Tick
       });


    var TickView = Backbone.View.extend({

          tagName: "span",

          initialize: function() {
            this.listenTo(this.model, "change", this.render);
            this.listenTo(this.model, "remove", this.remove)
          },

          render: function() {
            var put =this.model.get("put");
            var call = this.model.get("call");
            var priceClass ="text-muted";
            if(put>call) {
                priceClass="text-success";
            }
            else if(call>put){
                priceClass="text-danger";
            }
            this.$el.html(" <strong>"+this.model.get("nr")+ "</strong>: <span class='"+priceClass+"'>" + this.model.get("price") +"</span>@"+ put +"/"+ call );
            return this;
          }

        });

    var Ticker = Backbone.View.extend({

        el: $("#ticker"),

        initialize: function() {
            this.listenTo(this.model, 'add', this.addOne);
        },

        addOne: function(tick) {
              var view = new TickView({model: tick});
              this.$el.append(view.render().el);
        }

    });

        var ticks = new Ticks;

        var ticker = new Ticker({
                                    model: ticks
                                    });


    var Score = Backbone.Model.extend({
              idAttribute: "name"
    });

    var Scores = Backbone.Collection.extend({
         model: Score
       });

    var ScoreRow = Backbone.View.extend({

      tagName: "tr",

      initialize: function() {
        this.listenTo(this.model, "change", this.render);
        this.listenTo(this.model, "remove", this.remove)
      },

      render: function() {
        this.$el.html("<td>"+this.model.get("name") + "<td>"+this.model.get("score"));
        return this;
      }

    });

    var ScoresTable = Backbone.View.extend({

        el: $("#scores"),

        initialize: function() {
            this.listenTo(this.model, 'add', this.addOne);
        },

        addOne: function(score) {
              var view = new ScoreRow({model: score});
              this.$el.append(view.render().el);
        }

    });

    var scores = new Scores;

    var app = new ScoresTable({
                                model: scores
                                });


    var numberOfDatasets=40;
    var dataChart = [];
    var dataLabels = [];
    for(var i = 0; i<numberOfDatasets; i++){
        dataChart[i]=1000000;
        dataLabels[i]=-numberOfDatasets+i;
    }

    var data = {
        labels: dataLabels,
        datasets: [
                {
                     label: "WHX",
                     fillColor: "rgba(220,220,220,0.2)",
                     strokeColor: "rgba(220,0,220,1)",
                     pointColor: "rgba(,220,220,1)",
                     pointStrokeColor: "#fff",
                     pointHighlightFill: "#fff",
                     pointHighlightStroke: "rgba(220,220,220,1)",
                     data: dataChart
                 },
           ]
    };
    var ctx = document.getElementById("quotes").getContext("2d");

    var quotes = new Chart(ctx).Line(data, {
                                               bezierCurve: false,
                                               animation: false
                                           });

    var dateSocket = new WebSocket("ws://localhost:9000/ws/quotes");

    var receiveEvent = function(event) {
        var data = JSON.parse(event.data);

        ticks.add({nr: data.tick.nr, price: data.tick.price, put: data.tick.putVol, call: data.tick.callVol});
        if(ticks.length > 5 ){
            ticks.shift()
        }

        quotes.addData([data.tick.price], data.tick.nr);
        if(quotes.datasets[0].points.length > numberOfDatasets){
            quotes.removeData()
        }
        for (var broker in data.scores) {
            scores.set({name: broker, score: data.scores[broker]},{remove: false});
        }

    };

    dateSocket.onmessage = receiveEvent;


});
