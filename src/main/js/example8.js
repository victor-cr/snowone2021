const java = Java.type('Example8')
const {Worker} = require('worker_threads');

new Worker(
    /*language=js*/
    'Java.type("Example8").set(require("worker_threads").parentPort)',
    {eval: true}
).on('message', (id) => java.run(id))

require('express')()
    .get('/', (req, res) => {
        res.send('Hello from Javascript');
    })
    .get('/create/:topic', (req, res) => {
        java.createTopic(req.params.topic)
        res.send('Create topic: ' + req.params.topic);
    })
    .get('/subscribe/:topic', (req, res) => {
        java.subscribe('main', req.params.topic, (message) => console.log(message.value()));
        res.send('Subscribed to topic: ' + req.params.topic);
    })
    .get('/produce/:topic', (req, res) => {
        java.produce(req.params.topic, 'Hello from Kafka (' + new Date() + ')');
        res.send('Produced to topic: ' + req.params.topic);
    })
    .listen(8080);