var GroupProvider = require("../model/groupProvider.js").GroupProvider;
var groupProvider = new GroupProvider();

function chkPass(appio,appsocket,data)
{
	var groupname = data.groupdata.groupname;
	var password = data.password;
	
	var result = groupProvider.checkGroupPassword(groupname,password);
	
	appsocket.emit('passwordResult',{groupdata: data.groupdata, passResult: result});
}

exports.chkPass = chkPass;