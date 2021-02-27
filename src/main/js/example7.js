

const reader = (fileName) => String(require('fs').readFileSync(fileName))

const java = Java.type('Example7')
const {Worker} = require('worker_threads');

new Worker(
    /*language=js*/
    'Java.type("Example7").set(require("worker_threads").parentPort)',
    {eval: true}
).on('message', (id) => java.run(id))

java.start(reader);

setInterval(() => 1, 100)