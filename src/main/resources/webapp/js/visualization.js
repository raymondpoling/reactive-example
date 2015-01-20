$(function(){ // on dom ready

    var cy = cytoscape({
      container: document.getElementById('cy'),

      style: [
        {
          selector: 'node',
          css: {
            'content': 'data(id)',
            'text-valign': 'center',
            'text-halign': 'center'
          }
        },
        {
          selector: '$node > node',
          css: {
            'padding-top': '10px',
            'padding-left': '10px',
            'padding-bottom': '10px',
            'padding-right': '10px',
            'text-valign': 'top',
            'text-halign': 'center'
          }
        },
        {
          selector: 'edge',
          css: {
            'target-arrow-shape': 'triangle'
          }
        },
        {
          selector: ':selected',
          css: {
            'background-color': 'blue',
            'line-color': 'black',
            'target-arrow-color': 'black',
            'source-arrow-color': 'black'
          }
        },
        {
          selector: '.value',
          css: {
            'background-color': 'red',
            'line-color': 'black'
          }
        }
      ],

      elements: {
        nodes: [
          { data: { id: 'source'}}]},
//          ,
//          { data: { id: 'b' } },
//          { data: { id: 'c', parent: 'b' } },
//          { data: { id: 'd' } },
//          { data: { id: 'e' } },
//          { data: { id: 'f', parent: 'e' } }
//        ],
//        edges: [
//          { data: { id: 'ad', source: 'a', target: 'd' } },
//          { data: { id: 'eb', source: 'e', target: 'b' } }
//
//        ]
//      },

      layout: {
        name: 'dagre',
        padding: 5,
        columns: 10
      }
    });
    window.cy = cy
}); // on dom ready


var connection = new WebSocket('ws://localhost:8080/');


connection.onopen = function() {
    connection.send("register");
    console.log('Websocket up');

};

connection.onmessage = function(data) {
    var msg = JSON.parse(data.data);
    console.log("msg: " + msg.value)
    var x = cy.$("#" + msg.name);
    console.log(JSON.stringify(x.json()))
    if(x.json() == undefined) {
        cy.add({ group: "nodes",
                        data: { id: msg.name }});

    }
    if(msg.previous != "") {
              cy.add({ group: "edges", data: { id: msg.previous + msg.name, source: msg.previous, target: msg.name }});
            } else if(cy.$("#" + "source" + msg.name).json() == undefined) {
            cy.add({ group: "edges", data: { id: "source" + msg.name, source: "source", target: msg.name }});
            }
    if(msg.previous != "") {
        cy.remove(cy.$("#" + msg.value + msg.previous))
    }
    if(~msg.name.indexOf("sink")) {
    var children = cy.$("#" + msg.name).children();
        console.log(children.length);
        cy.remove(cy.$('#' + msg.value))
//            cy.remove(children);
//            cy.add({ group: "nodes",
//                                        data: { id: msg.value, parent: msg.name}});
    } else {
        if(cy.$('#' + msg.value)) {
            cy.add({ group: "nodes",
                                data: { id: msg.value}, classes: "value"});
        }
        cy.add({group: "edges", data: {id: msg.value + msg.name, source: msg.name, target: msg.value}})
    }
    cy.layout({name: 'dagre',
              padding: 5,
              columns: 10
            });


//                        cy.$("#a").show()
};

connection.onerror = function(data) {
    console.log('Websocket down');
};

connection.onclose = function(data) {
    console.log('Websocket closed');
};
