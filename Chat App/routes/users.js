var express = require('express');
var router = express.Router();
var GroupProvider = require("../model/groupProvider.js").GroupProvider;
var groupProvider = new GroupProvider();

router.get('/', function(req,res)
{
    res.render("users");
});


//This serves AJAX request to populate group dropdown
router.get('/populate',function(req,res){
    console.log("Sending groups for population");
    groupProvider.getGroups();
    var groups = groupProvider.retrieveGroups();
    console.log("Groups: " + groups);
    res.send(JSON.stringify(groups));
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

module.exports = router;