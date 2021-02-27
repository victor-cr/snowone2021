
console.log('==== GraalVM example #02 ====');
//Polyglot.eval('java', 'System.out.println("Java says: Hello");');
Java.type('Example2').greet();
const greetings = 'Hello too';
console.log('Javascript says: ' + greetings);
console.log('=============================');
