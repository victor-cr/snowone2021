require('express')()
    .get('/', (req, res) => {
        res.send('Hello from Javascript');
    })
    .get('/:name', (req, res) => {
        const fd = req.params.name;
        // if (fd.indexOf('..') < 0 && fd.indexOf('/') > 0) {
            res.send(Java.type('Example5').read(fd));
        // }
    })
    .listen(8080, () => console.log("Server started..."))
