var groupsList;
var PASSWORD = "admin";
window.onload = function()
{
	validatePassword(prompt("Please enter admin password:"));
	populateGroupsList();
}

function validatePassword(password)
{
	if(password === null)
	{
		alert("Leaving Admin Page");
		window.location.replace("http://localhost:3700/");
	}
	else if(password !== PASSWORD)
	{
		validatePassword(prompt("INCORRECT PASSWORD!!\n Please enter admin password:"));
	}
	else if(password === PASSWORD)
	{
		alert("Welcome to the admin page.\nClick refresh to see latest groups.");
	}
}

function setMaxGroups()
{
	console.log("setMaxGroups()");
	var input = document.getElementById("maxgroups");
	var max = input.value;
	
	if(max <= 0 || isNaN(max))
	{
		alert("Please enter a valid positive number");
	}
	else
	{
		users_xhr.onreadystatechange = setMaxCallback;
		users_xhr.open("GET",
					   window.location.href + "/setMax?" +
					   "attribute=groups" + "&" +
					   "max=" + max,
					   true);
		users_xhr.send(null);
	}
}

function setMaxUsers()
{
	var input = document.getElementById("maxusers");
	var max = input.value;
	
	if(max <= 0 || isNaN(max))
	{
		alert("Please enter a valid positive number");
	}
	else
	{
		console.log("setMaxUsers()");
		users_xhr.onreadystatechange = setMaxCallback;
		users_xhr.open("GET",
					   window.location.href + "/setMax?" +
					   "attribute=users" + "&" +
					   "max=" + max,
					   true);
		users_xhr.send(null);
	}
}

function setMaxCallback()
{
	console.log("setMaxCallback()");
	if(users_xhr.readyState == 4 && users_xhr.status == 200)
	{
		alert(users_xhr.responseText);
	}
}

function createTable(groupsString)
{
	console.log("createTable()");
	if(groupsString !== "")
	{ 
        resetTable();
		groupsList = JSON.parse(groupsString);
		
		for(var i = 0; i < groupsList.length; i++)
		{
			var groupname = groupsList[i].name;
			var members = groupsList[i].members;
			var owner = groupsList[i].owner;
			
			addRow(groupname,members,owner);
		}
	}
}

function resetTable()
{
	var table = document.getElementById("myTableData");
	var numRows = table.rows.length;
	
	for(var i = numRows-1; i > 0; i--)
	{
		table.deleteRow(i);
	}
}

function addRow(groupname,members,owner)
{
	console.log("addRow()");
	var table = document.getElementById("myTableData");
	
	var rowCount = table.rows.length;
	var row = table.insertRow(rowCount);
	
	row.insertCell(0).innerHTML = groupname;
	row.insertCell(1).innerHTML = userDropdown(members);
	row.insertCell(2).innerHTML = owner;
	row.insertCell(3).innerHTML = '<input type = "button" value = "Delete Group" onClick = "deleteGroup(this)">';
	row.insertCell(4).innerHTML = '<input type = "button" value = "Remove Selected User" onClick = "removeUser(this)">';
	
}

function userDropdown(members)
{
	var dropdownHTML = '<select id = "groupMembers">';
	
	for(var i = 0; i < members.length; i++)
	{
		dropdownHTML += '<option value="' + members[i].username + '">' + members[i].username + '</option>';
	}
	
	dropdownHTML += '</select>';
	
	return dropdownHTML;
}

function removeCallback()
{
	console.log("removeCallback()");
	if(users_xhr.readyState == 4 && users_xhr.status == 200)
	{
		alert(users_xhr.responseText);
		populateGroupsList();
	}
}

function deleteGroup(obj)
{
	var index = obj.parentNode.parentNode.rowIndex;
	var table = document.getElementById("myTableData");
	var cells = table.rows[index].cells;
	
	var groupname = cells[0].innerHTML;
	//alert("Cells: " + cells);
	/*alert("deleteGroup()\n" + 
		  "Name: " + cells[0].innerHTML + "\n" +
		  "Users: " + cells[1].childNodes[0].value + "\n" +
		  "Owner: " + cells[2].innerHTML);*/
	
	console.log("deleteGroup()");
	users_xhr.onreadystatechange = removeCallback;
	users_xhr.open("GET",
				   window.location.href + "/deleteGroup?" +
				   "groupname=" + groupname,
				   true);
	users_xhr.send(null);
}

function removeUser(obj)
{
	var index = obj.parentNode.parentNode.rowIndex;
	var table = document.getElementById("myTableData");
	var cells = table.rows[index].cells;
	
	var groupname = cells[0].innerHTML;
	var user = cells[1].childNodes[0].value;
	
	/*alert("removeUsers()\n" + 
		  "Name: " + cells[0].innerHTML + "\n" +
		  "Users: " + cells[1].childNodes[0].value + "\n" +
		  "Owner: " + cells[2].innerHTML);*/
	
	console.log("removeUser()");
	users_xhr.onreadystatechange = removeCallback;
	users_xhr.open("GET",
				   window.location.href + "/removeUser?" +
				   "groupname=" + groupname + "&" +
				   "user=" + user,
				   true);
	users_xhr.send(null);
}

var users_xhr = new XMLHttpRequest();
function callbackusers()
{
	console.log("callbackusers()");
	if(users_xhr.readyState == 4 && users_xhr.status == 200)
	{
		createTable(users_xhr.responseText);
	}
}

function populateGroupsList()
{
	console.log("populateGroupsList()");
	users_xhr.onreadystatechange = callbackusers;
	users_xhr.open("GET",
				   window.location.href + "/populateGroups",
				   true);
	users_xhr.send(null);
}