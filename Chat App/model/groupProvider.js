var fs = require('fs');
var path = require('path');

var jsonUrl = "/groups.json";
var jsonPath = path.resolve(__dirname + jsonUrl);

var jsonData;
var curGroups;
var MAXGROUPS;
var MAXUSERS;

//function to create group object
function Group(groupname,owner,members,ownersocketid,password)
{
    this.name = groupname;
    this.owner = owner;
    this.members = members;
    this.ownersocketid = ownersocketid
	this.password = password;
}

GroupProvider = function()
{
    this.getGroups();
    console.log("Current groups: " + curGroups);
};

GroupProvider.prototype.getGroups = function()
{
    fs.readFile(jsonPath,'utf8',function(err,jsonString)
    {
        if(err) throw err;
        console.log("jsonString was retrieved");
      
        updateGroups(jsonString);
    });
};

function updateGroups(jsonString)
{
	if(jsonString)
	{
    	jsonData = JSON.parse(jsonString);
  
    	curGroups = jsonData.groupsList;
    	MAXGROUPS = jsonData.maxGroups;
		MAXUSERS = jsonData.maxUsers;
	}
}

GroupProvider.prototype.removeMember = function(groupname, useridremove)
{
	console.log("removeMember()");
	for(var i = 0; i < curGroups.length; i++)
	{
		console.log(curGroups.length);
		if(curGroups[i].name === groupname)
		{
			console.log("found group to remove member from");
			useridremove = this.getRecipientId(useridremove,curGroups[i]);
			if(curGroups[i].members.length === 1)
			{ 
				this.removeGroup(groupname);
				break;
			}
			else
			{
				for(var j = 0; j < curGroups[i].members.length; j++)
				{
					if(curGroups[i].members[j].userid === useridremove) 
					{
						if(curGroups[i].members[j].userid === curGroups[i].ownersocketid) 
						{
							curGroups[i].owner = curGroups[i].members[j+1].username; 
							curGroups[i].ownersocketid = curGroups[i].members[j+1].userid;
						}
						curGroups[i].members.splice(j, 1); 
						break;
					}
				}
			}
		}
	}
	
	saveData();
};

GroupProvider.prototype.removeGroup = function(groupname)
{
	console.log("removeGroup()");
	for(var i = 0; i < curGroups.length; i++)
    {
        if(curGroups[i].name === groupname)
        {
			curGroups.splice(i, 1); 
			break;
		}
	}
	
	saveData();
};

GroupProvider.prototype.retrieveGroups = function()
{
    return curGroups;
};

GroupProvider.prototype.getMaxGroups = function()
{
	return MAXGROUPS;
};

GroupProvider.prototype.getMaxUsers = function()
{
	return MAXUSERS;
};

GroupProvider.prototype.setMaxGroups = function(maxGroups)
{
	if(maxGroups < curGroups.length)
	{
		return curGroups.length;
	}
	else
	{
		MAXGROUPS = maxGroups;
		saveData();
		return -1;
	}
};

function maxUsersInSystem()
{
	var max = 0;
	for(var i = 0; i < curGroups.length; i++)
	{
		if(curGroups[i].members.length > max)
		{
			max = curGroups[i].members.length;
		}
	}
	
	return max;
}

GroupProvider.prototype.setMaxUsers = function(maxUsers)
{
	if(maxUsers < maxUsersInSystem())
	{
		return maxUsersInSystem();
	}
	else
	{
		MAXUSERS = maxUsers;
		saveData();
		return -1;
	}
};

GroupProvider.prototype.checkGroupPassword = function(groupname,password)
{
	for(var i = 0; i < curGroups.length; i++)
	{
		if(curGroups[i].name === groupname)
		{
			return password === curGroups[i].password;
		}
	}
};

GroupProvider.prototype.addNewGroup = function(groupname,owner,members,ownersocketid,password)
{
    var newGroup = new Group(groupname,owner,members,ownersocketid,password);
    
    curGroups.push(newGroup);
    
    saveData();
    
    this.getGroups();
    
    console.log("New group added");
};

function saveData()
{
    jsonData.groupsList = curGroups;
	jsonData.maxGroups = MAXGROUPS;
	jsonData.maxUsers = MAXUSERS;
	
	jsonString = JSON.stringify(jsonData);
	
    fs.writeFile(jsonPath,jsonString,function(err)
    {
        if (err) throw err;

        console.log("JSON file updated");
    });
}

GroupProvider.prototype.userExists = function(group,username)
{
    for(var i = 0; i < group.members.length; i++)
    {
        if(group.members[i].username === username)
        {
            return true;
        }
    }
    
    return false;
};

GroupProvider.prototype.getRecipientId = function(recipient,group)
{
	console.log("getRecipientId()");
    for(var i = 0; i < group.members.length; i++)
    {
        if(group.members[i].username === recipient)
        {
            return group.members[i].userid;
        }
    }
    
    return null;
};

GroupProvider.prototype.addNewMember = function(groupname, userdata)
{   
    for(var i = 0; i < curGroups.length; i++)
    {
        if(curGroups[i].name === groupname)
        {
            //var user = {username: userdata.username, userid: userdata.socketid};
            
            curGroups[i].members.push(userdata);
            break;
        }
    }
    
    saveData();
    this.getGroups();
};
        
GroupProvider.prototype.groupExists = function(groupname)
{
    for(var i = 0; i < curGroups.length; i++)
    {
        if(curGroups[i].name === groupname)
        {
            return true;
        }
    }
    
    return false;
};

console.log("Group Provider is a : " + typeof GroupProvider)

exports.GroupProvider = GroupProvider;