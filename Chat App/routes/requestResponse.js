var GroupProvider = require("../model/groupProvider.js").GroupProvider;
var groupProvider = new GroupProvider();


function reqResHand(appio, appsocket, data)
{
    console.log("Received request response from owner");
    var joined_ = false;
    var jsonString = JSON.stringify(groupProvider.retrieveGroups());
    
    if(data.accept)
    {
        joined_ = true;
        var userdata = {username: data.userdata.username, userid: data.userdata.socketid};
        groupProvider.addNewMember(data.userdata.groupname, userdata);
        
        jsonString = JSON.stringify(groupProvider.retrieveGroups());
        
        console.log("sending groupname: " + data.userdata.groupname);
        appsocket.broadcast.to(data.userdata.socketid).emit('joinedResult',{json: jsonString, joined: joined_, groupname: data.userdata.groupname});
        //socket.join(data.userdata.groupname);
    }
    else
    {
        
        appsocket.broadcast.to(data.userdata.socketid).emit('joinedResult',{json: jsonString, joined: joined_, userdata: data});
    }
}

exports.reqResHand = reqResHand;