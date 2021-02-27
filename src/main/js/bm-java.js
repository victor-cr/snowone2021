Java.type('Helper').init(require);

const benchmark = require('./bm')

const readDirectoryTreeAsync = Java.type('Bm').readDirectoryTreeAsync;
const readDirectoryTreeSync = Java.type('Bm').readDirectoryTreeSync;
const readFileAsync = Java.type('Bm').readFileAsync;
const readFileSync = Java.type('Bm').readFileSync;
const matrixSync = Java.type('Bm').matrixSync;

benchmark({
    name: 'Java',
    file: 'java',
    readDirectoryTreeAsync,
    readDirectoryTreeSync,
    readFileAsync,
    readFileSync,
    matrixSync
})