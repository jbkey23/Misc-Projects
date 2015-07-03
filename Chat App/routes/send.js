var GroupProvider = require("../model/groupProvider.js").GroupProvider;
var groupProvider = new GroupProvider();

function sendHandler(appio,appsocket,data)
{
    console.log(data.curgroup.name);
    console.log(data.recipient);
    if(data.recipient === "default")
    {
        console.log("sending to all");
        appio.to(data.curgroup.name).emit('message',data);
    }
    else
    {
        var recipientid = groupProvider.getRecipientId(data.recipient,data.curgroup);
        console.log("sending to one: " + recipientid);
        appsocket.broadcast.to(recipientid).emit('message',data);
    }
}

exports.sendHandler = sendHandler;