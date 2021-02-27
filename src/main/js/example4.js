
const reader = (fileName) => String(require('fs').readFileSync(fileName))
console.log('==== GraalVM example #04 ====');
Java.type('Example4').read(new Array(48,49,50), reader)
console.log('=============================');
