

const reader = (fileName) => String(require('fs').readFileSync(fileName))

Java.type('Example6').start(reader);

setInterval(() => 1, 100)