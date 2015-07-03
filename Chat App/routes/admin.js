var express = require('express');
var router = express.Router();
var GroupProvider = require("../model/groupProvider.js").GroupProvider;
var groupProvider = new GroupProvider();

router.get('/', function(req,res)
{
    res.render("admin");
});


//This serves AJAX request to populate group table
router.get('/populateGroups',function(req,res){
    console.log("Sending groups for population");
    groupProvider.getGroups();
    var groups = groupProvider.retrieveGroups();
    console.log("Groups: " + groups);
    res.send(JSON.stringify(groups));
});

//This serves AJAX request to delete a group
router.get('/deleteGroup',function(req,res){
	console.log("Delete Group from system");
    groupProvider.getGroups();
	var groupname = req.query.groupname;
    groupProvider.removeGroup(groupname);
	
    res.send(groupname + " removed from the system");
});

//This serves AJAX request to remove a user
router.get('/removeUser',function(req,res){
	console.log("Remove user from group");
    groupProvider.getGroups();
    
	var groupname = req.query.groupname;
	var user = req.query.user;
	
	groupProvider.removeMember(groupname,user);
	
    res.send(user + " removed from " + groupname);
});

//This serves AJAX request to set a max attribute
router.get('/setMax',function(req,res){
	console.log("Set Max Attribute");
	groupProvider.getGroups();
	
	var attribute = req.query.attribute;
	var max = req.query.max;
	var success;
	
	if(attribute === "users")
	{
		success = groupProvider.setMaxUsers(Number(max));
	}
	else if(attribute === "groups")
	{
		success = groupProvider.setMaxGroups(Number(max));
	}

	if(success === -1)
	{
		res.send("Max number of " + attribute + " is now: " + max);
	}
	else
	{
		res.send("No change made.\nMore " + attribute + " already in system.\nPlease enter number higher than " + success);
	}
});

module.exports = router;