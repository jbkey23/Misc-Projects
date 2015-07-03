var GroupProvider = require("../model/groupProvider.js").GroupProvider;
var groupProvider = new GroupProvider();

function joinHandler(appio,appsocket,data)
{
    //console.log(groupProvider);
    console.log("In join method");
    if(data.newgroup === true)
    {
        console.log("new group");
        var name = data.groupname;
        var owner = data.username;
        var id = data.socketid;
        var user = {username: owner, userid: id};
		var password = data.password;
        
        groupProvider.addNewGroup(name,owner,[user],id,password);
        
        jsonString = JSON.stringify(groupProvider.retrieveGroups());
        
        appsocket.emit('joined', {json: jsonString, newgroup: true});

        appsocket.join(data.groupname);
    }
    else
    {
        var groups = groupProvider.retrieveGroups();

        for(var i = 0; i < groups.length; i++)
        {
            if(groups[i].name === data.groupname)
            {
                if(!groupProvider.userExists(groups[i],data.username))
                {
                    console.log("broadcasting from " + data.socketid + " to " + groups[i].ownersocketid); 
                    appsocket.broadcast.to(groups[i].ownersocketid).emit('joinRequest',data);
                    break;
                }
                else
                {
                    appsocket.emit('joinError',data);
                    break;
                }
            }
        }
    }
}

exports.joinHandler = joinHandler;