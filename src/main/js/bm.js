const benny = require('benny');

function benchmark(context) {
    const options = {
        initCount: 100,
        minSamples: 10,
        maxTime: 20,
    }

    return benny.suite(
        context.name,
        benny.add('Async traverse', async () => await context.readDirectoryTreeAsync('.'), options),
        benny.add('Sync traverse', () => context.readDirectoryTreeSync('.'), options),
        benny.add('Async read file', async () => await context.readFileAsync('target/lib/js-21.0.0.2.jar'), options),
        benny.add('Sync read file', () => context.readFileSync('target/lib/js-21.0.0.2.jar', 1024), options),
        benny.add('Matrix multiply', () => context.matrixSync(200), options),
        benny.cycle(),
        benny.complete(),
        benny.save({file: context.file, details: true, format: 'json'}),
    );
}

module.exports = benchmark