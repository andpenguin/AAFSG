const port = "80" //Port for connecting to the website
//Importing JS libraries for file reading, server launching, and favicon
var express = require('express');
var app = express();
var server = require("http").createServer(app)
var position = 0;
const favicon = require('serve-favicon');
var io = require('socket.io')(server, {
    cors: {
        origin: "*"
    }
});
const fs = require("fs");
const child = require("child_process");

app.set('view engine', 'ejs') // Initializes ejs library
app.use(favicon('./views/favicon.ico')) // Displays favicon image file

app.get("/", (req, res) => { // Renders the front page when a user loads into the main domain
    res.render("front");
});

app.use(function(req, res, next) { // If any other page is accessed
    res.status(404).render("404err") // Display the 404 error page
});

server.listen(port, () => { // Start listening for clients when the server is launched
    console.log("Server is running at Port: " + port) // log the server port
    try {
        child.exec('sudo gradle run')
    }
    catch {
        console.log("Child process failed")
    }
});

io.on("connection", (socket) => { // When a client connects
    socket.on("generate", () => { // When the server is pinged to check the status of resetting
        sendSeed(socket)
    });
});

function sendSeed(socket) {
    var seed = ''
    fs.readFile("./src/main/java/and_penguin/seeds.txt", "utf8", (err, seeds) => {
       if (err)
            console.log(err)
       if (position >= seeds.length) {
            socket.emit("seed", "Generating... Click again in a minute")
            return
       }
       var lineEnd = 0;
       for (i = position; i < seeds.length; i++)
            if (seeds[i] === '\n') {
                lineEnd = i-position;
                break;
            }
       for (i = position; i < lineEnd+position; i++)
            seed += seeds[i]
       position += lineEnd+1;
       socket.emit("seed", seed)
    });
}