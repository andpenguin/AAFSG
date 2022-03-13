const port = 3000 //Port for connecting to the website
//Importing JS libraries
const express = require('express');
const https = require('https');
const fs = require('fs');

var key = fs.readFileSync(__dirname + '/../certs/selfsigned.key');
var cert = fs.readFileSync(__dirname + '/../certs/selfsigned.crt');
var options = {
  key: key,
  cert: cert
};

app = express()
var server = https.createServer(options, app);

const child = require("child_process");
const favicon = require('serve-favicon');
var io = require('socket.io')(server, {
    cors: {
        origin: "*"
    }
});

var authorized = false
var password
var position = -1
var latestDate = new Date();

app.set('view engine', 'ejs') // Initializes ejs library
app.use(favicon('./views/favicon.ico')) // Displays favicon image file

app.get("/", (req, res) => { // Renders the front page when a user loads into the main domain
    res.render("front");
});

app.get("/stats", (req, res) => {
    res.render("stats");
});

app.get("/verify", (req, res) => {
    if (authorized) {
        res.render("verify")
        authorized = false
    }
    else
        res.status(404).render("404err")
});
app.use(function(req, res, next) { // If any other page is accessed
    res.status(404).render("404err") // Display the 404 error page
});

server.listen(port, () => { // Start listening for clients when the server is launched
    if (process.argv[2] = "-p" && process.argv.length > 3) // Check for custom password args
        password = process.argv[3]
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
    socket.on("statRequest", () => {
        sendStats(socket)
    });
    socket.on("verify", seed => {
        sendDate(socket, seed)
    });
    socket.on("password", pass => {
        if (pass === password) {
            authorized = true
            socket.emit("passCorrect")
        }
    });
    socket.on("splashText", () => {
        sendText(socket)
    });
});

function sendSeed(socket) {
    var seconds = (new Date().getTime() - latestDate.getTime()) / 1000
    if (seconds < 5) {
        socket.emit("seed", "Wait for the server rate limit of 5 seconds")
        return
    }
    fs.readFile("./src/main/java/and_penguin/seeds.txt", "utf8", (err, seeds) => {
       if (err)
            console.log(err)
       var newPos = seeds.indexOf('\n', position+1)
       if (newPos == -1) {
           socket.emit("seed", "Generating... Click again in a minute")
           return
       }
       var seed = seeds.substring(position+1, newPos)
       position = newPos
       var date = new Date().toUTCString()
       var data = "Seed: " + seed + "  - Time Shown to user " + date + "\n"
       fs.appendFile("./js/logs.txt", data, (err) => {
            if (err)
                console.log(err)
       });
       socket.emit("seed", seed)
       latestDate = new Date()
    });
}

function sendStats(socket) {
    var stats = [0,0]
    fs.readFile("./src/main/java/and_penguin/seeds.txt", "utf8", (err, seeds) => {
        if (err)
            console.log(err)
       index = seeds.indexOf('\n');
       while (index != -1) {
            if (index > position)
                stats[1]++
            stats[0]++
            index = seeds.indexOf('\n', index+1)
       }
       socket.emit("stats", stats)
    });
}

function sendDate(socket, seed) {
    fs.readFile("./js/logs.txt", "utf8", (err, logs) => {
        if (err)
            console.log(err)
        var start = logs.indexOf(seed)
        var end = logs.indexOf("\n", start)
        var line = logs.substring(start, end)
        if (start == -1)
            line = "Seed not found. This is either a seed not generated by this filter, or an internal bug"
        socket.emit("date", line)
        console.log(start + " " + end)
    });
}

function sendText(socket) {
    fs.readFile("./js/splashtexts.txt", "utf8", (err, texts) => {
        if (err)
            console.log(err)
        var numTexts = 0
        var index = 0
        while (texts.indexOf('\n', index) != -1) {
            index = texts.indexOf('\n', index)  + 1
            numTexts++;
        }
        var splashNum = Math.floor(Math.random() * numTexts)
        index = -1
        for (i = 0; i < splashNum; i++)
            index = texts.indexOf('\n', index+1)
        index++
        var splashText = texts.substring(index, texts.indexOf('\n', index))
        socket.emit("splash", splashText)
    });
}