
const reader = (fileName) => String(require('fs').readFileSync(fileName))
console.log('==== GraalVM example #04 ====');
Java.type('Example4').read("src/main/java/Example4.java", reader)
console.log('=============================');
