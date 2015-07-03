#!/usr/bin/env nodejs

// First line above(called as hashbang or shebang or #!)
// It makes this script to run as a server-side Javascript(i.e. node.js)



// Node.js Framework (Express) settings
var express = require("express");
var fs = require("fs");
var path = require("path");
var app = express();
var port = 3700;
var reqResHand = require("./routes/requestResponse");
var joinHandler = require("./routes/join");
var sendHandler = require("./routes/send");
var joinGrpHand = require("./routes/joinGroup");
var chkPass = require("./routes/checkPassword");
var alertRm = require("./routes/alertRemove");
var users = require("./routes/users");
var admin = require("./routes/admin");

 
// HTML Template Engine (JADE) settings
app.set('view engine', "jade");
app.engine('jade', require('jade').__express);
/*
app.get("/", function(req, res){
    res.render("chat_client");
});
*/

app.use(express.static(path.join(__dirname, 'public')));

app.use('/', users);
app.use('/admin', admin);

// Websocket (via socket.io) settings
var io = require('socket.io').listen(app.listen(port));
io.sockets.on('connection', function (socket) {
    console.log("Socket Connection Established..");
    
    socket.emit('message', { message: 'welcome to the Chat' });
    
    
    socket.on('send', function (data) {
        sendHandler.sendHandler(io,socket,data);
    });
    socket.on('join',function(data)
    {
        joinHandler.joinHandler(io,socket,data);
    });
    socket.on('requestResponse', function(data)
    {
       reqResHand.reqResHand(io,socket,data);
    });
    socket.on('joinGroup', function(data)
    {
        joinGrpHand.joinGrpHand(io,socket,data);
    });
	socket.on('checkPassword',function(data)
	{
		chkPass.chkPass(io,socket,data);
	});
	socket.on('alertRemoved',function(data)
	{
		alertRm.alertRm(io,socket,data);
	});
});


console.log("Listening on port " + port);
