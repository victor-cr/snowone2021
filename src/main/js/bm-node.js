const benchmark = require('./bm')
const fs = require('fs');
const util = require('util');

const lstat = util.promisify(fs.lstat);
const readdir = util.promisify(fs.readdir);

async function readDirectoryTreeAsync(root) {
    const stat = await lstat(root);

    if (stat.isDirectory()) {
        const files = await readdir(root);
        const names = await Promise.all(files.map(file => readDirectoryTreeAsync(root + '/' + file)))

        return [root, ...names.flatMap(e => e)];
    } else {
        return [root];
    }
}

function readDirectoryTreeSync(root) {
    const stat = fs.lstatSync(root);

    if (stat.isDirectory()) {
        const files = fs.readdirSync(root);
        const names = files.map(file => readDirectoryTreeSync(root + '/' + file))

        return [root, ...names.flatMap(e => e)];
    } else {
        return [root];
    }
}

async function readFileAsync(fileName) {
    return new Promise((resolve, reject) => {
        const stream = fs.createReadStream(fileName)
        let result = 0;

        stream.on("data", chunk => {
            for (const byte of chunk.values()) {
                if (byte === 0) {
                    result++;
                }
            }
        }).on('error', reject).on('end', () => resolve(result))
    })
}

function readFileSync(fileName, bufferSize) {
    const buffer = Buffer.alloc(bufferSize);
    const fd = fs.openSync(fileName)
    let read, result = 0, position = 0;

    while ((read = fs.readSync(fd, buffer, 0, bufferSize, position)) > 0) {
        position += read;

        for (let i = 0; i < read; i++) {
            if (buffer.readInt8(i) === 0) {
                result++;
            }
        }
    }

    return result;
}

function matrixSync(matrixSize) {
    let length = matrixSize * matrixSize;

    const left = new Array(length);
    const right = new Array(length);
    const result = new Array(length);

    for (let i = 0; i < length; i++) {
        left[i] = Math.random() * length;
        right[i] = Math.random() * length;
        result[i] = 0;
    }

    for (let row = 0; row < matrixSize; row++) {
        for (let col = 0; col < matrixSize; col++) {
            const offset = row * matrixSize
            const x = row * matrixSize + col;

            for (let i = 0; i < matrixSize; i++) {
                result[x] += left[offset + i] * right[i * matrixSize + col];
            }
        }
    }

    return result;
}

benchmark({
    name: 'Node.JS',
    file: 'nodejs',
    readDirectoryTreeAsync,
    readDirectoryTreeSync,
    readFileAsync,
    readFileSync,
    matrixSync
})