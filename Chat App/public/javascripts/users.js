window.onload = function() { 
    var messages = [];
    var socket = io.connect(window.location.href);
    var message = document.getElementById("message");
    var sendbutton = document.getElementById("sendbutton");
    var chatbox = document.getElementById("chatbox");
    var username = document.getElementById("username");
    
    var sendsound = document.getElementById("sendsound");
	var recsound = document.getElementById("recsound"); 
	var sendselector = document.getElementById("sendselector"); 
	var recselector = document.getElementById("recselector"); 
	
	// Ugly code that (should) get the job done
	var sendMP3 = document.getElementById("sendMP3"); // send filename 
	var sendOGG = document.getElementById("sendOGG"); 
	var recMP3 = document.getElementById("recMP3"); // receive filename
	var recOGG = document.getElementById("recOGG"); 
   
    var colorpicker = document.getElementById("colorpicker");
    var italicbutton = document.getElementById("italicbutton");
    var boldbutton = document.getElementById("boldbutton");
    var increasefont = document.getElementById("increasefont");
    var decreasefont = document.getElementById("decreasefont");
    var fontselector = document.getElementById("fontselector");
    var italic_ = false;    //default value
    var bold_ = false;  //default value
    var fontcolor_ = "#000000";   //default value
    var fontsize_ = "15px"; //default value
    var fonttype_ = "Arial, Helvetica, sans-serif"; //default value
    
    var joingroup = document.getElementById("joingroup");
    var groupselector = document.getElementById("group");
    var groupname = document.getElementById("groupname");
    var groupmembers = document.getElementById("groupmembers");
    var curGroupList = [];
    var curGroup = {};
    var selectedGroupIndex = 0;
    var selectedMemberIndex = 0;
    
    var msgHistory = document.getElementById("historycount");
    var setHistory = document.getElementById("historybutton");
    var num_msg = 0; //default
	
	var leavechatbutton = document.getElementById("leavechat"); 
	var expelmemberselector = document.getElementById("removeuserssel");
	var expelmemberbutton = document.getElementById("removeusersbut");
	var expelmembertitle = document.getElementById("removetitle"); 
	var expelmemberindex = 0; 
	
    leavechatbutton.onclick = function()
    {
		if(confirm("Are you sure you want to leave?"))
		{
        	removeUser(username.value); 
			alert("Goodbye");
			window.location.replace("http://localhost:3700/");
			
		}
    }
	
    expelmemberbutton.onclick = function()
    {
		 var uname = expelmemberselector.options[expelmemberindex].value;
         removeUser(uname);
		 var user = getUser(uname);
		 socket.emit('alertRemoved',user);
    }
	
    expelmemberselector.onchange = function()
    {
        expelmemberindex = expelmemberselector.selectedIndex;
    }
    
	socket.on('removeAlert', function(data)
	{
		alert("You have been removed from the group.\nGoodbye");
		window.location.replace("http://localhost:3700/");
	});
	
	function getUser(uname)
	{
		for(var i = 0; i < curGroup.members.length; i++)
		{
			if(curGroup.members[i].username === uname)
			{
				return curGroup.members[i];
			}
		}
	}
		
	function ownerCheck()
    {
       if(curGroup.owner === username.value)
	   { 
            expelmemberselector.style.visibility = "visible";
			expelmemberbutton.style.visibility = "visible";
            expelmembertitle.style.visibility = "visible";
	   }
	   else
	   {
            expelmemberselector.style.visibility = "hidden";
            expelmemberbutton.style.visibility = "hidden";
            expelmembertitle.style.visibility = "hidden";
       }
    }
    
    socket.on('message', function (data) 
    {
        console.log("Received Message");
        chatData = data;
        // Check username for illegal characters
			var filter = /^([a-zA-Z0-9])+$/; 
			var username_check = filter.test(data.username);
			
			// Check for message length
			var len_check = data.message.length < 200; 
        
        if(data.message && len_check && username_check)
	    {
            // Message received I think	
            // you're going to hear the horse twice; trust me it works here
            //alert("Receive sound should play (I think)"); 
            recsound.load();
            recsound.play();
            messages.push(data);
         
            printMessages();
        } 
        else
        {
            if(!len_check) { alert("Maximum message length is 200. Your msg length is: " +  data.message.length); }

            if(!username_check) { alert ("Please use only alphanumeric characters. No @,#,%, etc. please"); } 
            console.log("Received message is not well-formatted:", data);
        }
    });
 
    function printMessages()
    {
        var html = '';
            
            var temp = messages.length;
            
            if(num_msg > 0 && num_msg < temp)
            {
                temp = num_msg;
            }
            
            for(var i=messages.length-temp; i<messages.length; i++) 
	        {
                var italic, bold;
                if(messages[i].italic === true)
                    italic = "italic";
                else
                    italic = "normal";
                
                if(messages[i].bold === true)
                    bold = "bold";
                else
                    bold = "normal";
                
                html += '<b>' + (messages[i].username ? messages[i].username : 'Server');
                html += (messages[i].curgroup ? '@' + messages[i].curgroup.name + '@' + messages[i].curgroup.owner + ': </b>' : ': </b>');
                
                html += '<span style=color:' + messages[i].fontcolor +
                                ';font-size:' + messages[i].fontsize +
                                ';font-type:' + messages[i].fonttype +
                                ';font-style:' + italic +
                                ';font-weight:' + bold + '>' + messages[i].message + '</span><br />';
            }
            chatbox.innerHTML = html;
        chatbox.scrollTop = chatbox.scrollHeight;
    }
    
    setHistory.onclick = function()
    {
        history();
        printMessages();
    }
    
    socket.on('joined', function(data)
    {
        console.log("in joined socket");
        curGroupList = JSON.parse(data.json);
    
        if(data.newgroup)
            selectedGroupIndex = curGroupList.length;
        
        console.log(selectedGroupIndex);
        groupname.style.visibility = "hidden";  //hide groupname input box
        username.disabled = true;   //disabled username input; can't change username
       
        populateDropdown(data.json);
        getCurGroup();
        populateMembers();
		ownerCheck();
        
    });
    
    //handles some error that happened with joining a group was attempted
    socket.on('joinError', function(data)
    {
        alert("Username (" + data.username + ") already exists in group (" + data.groupname + ")!\n Please enter a different username!");
    });
    
    //handle request from user to join owners group
    socket.on('joinRequest',function(data)
    {
        var accept_ = false;
        if(confirm(data.username + " has requested to join your group!\n Click 'OK' to allow or 'Cancel' to reject."))
        {
            accept_ = true;
        }
        console.log("broadcasting back to socket id: " + data.socketid);
        
        //sendJoinRequestResponse(data,accept_);
        console.log("accept: " + accept_);
        socket.emit('requestResponse', {userdata: data,accept:accept_});
    });
    
    /*//send back the response from owner to user about joining the group
    function sendJoinRequestResponse(data,accept_)
    {
        console.log("sending back response");
        socket.emit('requestResponse',{userdata: data, accept: accept_});
    }*/
    
    //handles result from asking owner to join group
    socket.on('joinedResult', function(data)
    {
        console.log("made it back to joinedResult finally");
        if(data.joined)
        {
            console.log("group is: " + data.groupname);
            socket.emit('joinGroup', data.groupname)
            console.log("Request to join accepted");
            
            alert("Welcome to the group!");
            curGroupList = JSON.parse(data.json);
            
            groupname.style.visibility = "hidden";  //hide groupname input box
            username.disabled = true;   //disabled username input; can't change username
            
            populateDropdown(data.json);
            getCurGroup();
            populateMembers();
        }
        else
        {
            console.log("Request to join denied");
            alert("You're request to join has been denied by the group owner!!");
        }
    });
              
	var users_xhr = new XMLHttpRequest();
    function removeCallback()
    {
        console.log("callbackusers()");
        if(users_xhr.readyState == 4 && users_xhr.status == 200)
        {
			 //alert(users_xhr.responseText);
             populateMembers(); 
			 ownerCheck(); 
        }
    }
	
    function removeUser(usern)
    {
        console.log("removeUser()");
        users_xhr.onreadystatechange = removeCallback;
        users_xhr.open("GET",
                   window.location.href + "removeUser?" +
                   "groupname=" + curGroup.name + "&" +
                   "user=" + usern,
                   true);
        users_xhr.send(null);
    }
	
    window.onfocus = function()
    {
        populateMembers();
		ownerCheck();
    }
    
    window.onblur = function()
    {
        populateMembers();
		ownerCheck();
    }
    
    //populate group members dropdown
    function populateMembers()
    {
        getCurJsonGroups();
        getCurGroup();
		var sendDefault = '<option value="default">All Group Members</option>';
		var removeDefault = '<option value="default">None</option>'
        var membersoptions = '';
	 
        for(var i = 0; i < curGroup.members.length; i++)
        {
                membersoptions += '<option value="' + curGroup.members[i].username + '">' + curGroup.members[i].username + '</option>';
        }
        
            groupmembers.innerHTML = sendDefault + membersoptions;   
			expelmemberselector.innerHTML = removeDefault + membersoptions;
    }
    
    //get current group
    function getCurGroup()
    {
        for(var i = 0; i < curGroupList.length; i++)
        {
            if(curGroupList[i].name === groupselector.options[selectedGroupIndex].value)
            {
                curGroup = curGroupList[i];
                break;
            }
            
        }
    }
    
    //helper function for testing name conditions
    var testname = function(name) 
    {
			var filter = /^([a-zA-Z0-9])+$/; 
			return filter.test(name);
    }
    
    //check if group already exists
    function groupExists(gn)
    {
        for(var i = 0; i < curGroupList.length; i++)
        {
            if(curGroupList[i].name === gn)
            {
                return true;
            }
        }
        
        return false;
    }
    
	socket.on('passwordResult',function(data)
	{
		var groupdata = data.groupdata;
		var result = data.passResult;
		
		if(result)
		{
			socket.emit('join',groupdata);
		}
		else
		{
			alert("You provided the wrong password.\nRequest to join DENIED!!");
		}
	});
	
    function addNewGroup (userName, groupName, newGroup, socketId)
    {
		if(newGroup)
		{
			var groupPassword = prompt("Please enter a password for the group.");
			socket.emit('join',{groupname: groupName, username: userName, newgroup: newGroup, socketid: socketId, password: groupPassword});
		}
		else
		{
			var groupPassword = prompt("Password required to join:");
			var groupData = {groupname: groupName, username: userName, newgroup: newGroup, socketid: socketId};
			socket.emit('checkPassword',{groupdata: groupData, password: groupPassword});
		}
    }
    
    joingroup.onclick = function()
    {
       
        var userName = username.value;
        if(groupselector.selectedIndex === 0)
        {
            var groupName = groupname.value;
            if(!testname(groupName))
            {
                alert("Group name not allowed!!");
            }
            else if(!testname(userName))
            {
                alert("Must have a valid username!!");
            }
            else
            {
                if(groupExists(groupName))
                {
                    alert("Group already exists!!");
                }
                else
                {
                    addNewGroup(userName, groupName,true,socket.id);
                }
            }
        }
        else
        {
            var groupName = groupselector.options[selectedGroupIndex].value;
            if(!testname(userName))
            {
                alert("Must have a valid username!!");
            }
            else
            {
                addNewGroup(userName,groupName,false,socket.id);
            }
        }
    };
    
    groupname.onclick = function()
    {
        getCurJsonGroups();
    }
    
    groupmembers.onchange = function()
    {
        selectedMemberIndex = groupmembers.selectedIndex;
    }
    
    function getCurJsonGroups()
    {
        var xhr = new XMLHttpRequest();
        
        //check for failures
        if(!xhr)
        {
            alert('Failed to create a XMLHttpRequest');
            return false;
        }
        
        //check if browser supports access-control-allow-origin
        if(!('withCredentials' in xhr))
        {
            alert('Browser does not support CORS.');
            return false;
        }
        
        xhr.onreadystatechange = callback;
        xhr.open("GET",
                 window.location.href + "populate",
                 true);
        xhr.send(null);
        
        function callback()
        {
            try
            {
                if (xhr.readyState === 4)
                {
                    if(xhr.status === 200)
                    {
                        if(xhr.responseText !== "")
                        {
                            populateDropdown(xhr.responseText); 
                        }
                        //alert("JsonString: " + xhr.responseText);
                        
                    }
                    else
                    {
                        alert("Problem with the request: status = " + xhr.status);
                    }
                }
            }
            catch (e)
            {
                alert("Exception: " + e.description);
            }
        }
    }
    //fetch groups and populate dropdown
    groupselector.onclick = function()
    {
        getCurJsonGroups();
    }
    
    groupselector.onchange = function()
    {
        selectedGroupIndex = groupselector.selectedIndex;
        if(selectedGroupIndex > 0)
            groupname.style.visibility = "hidden";
        else
            groupname.style.visibility = "visible";
    }
    
    //populates group dropdown based on groups in json file
    function populateDropdown(jsonText)
    {
        curGroupList = JSON.parse(jsonText);
        var groupoptions = '<option value="default">Create a New Group</option>' ;
        for(var i = 0; i < curGroupList.length; i++)
        {

            groupoptions += '<option value="' + curGroupList[i].name + '">' + curGroupList[i].name + '</option>';
        }
        
        groupselector.innerHTML = groupoptions;
        groupselector.selectedIndex = selectedGroupIndex;
    }
    

    
    sendselector.onchange = function()
    {
        selectedSend = sendselector.options[sendselector.selectedIndex].value;
        // selectedSend = sendX where X is some number in Jade
        sendMP3.src="/audio/" + selectedSend + ".mp3"; 
        sendOGG.src="/audio/" + selectedSend + ".ogg"; 
        //alert(sendMP3.src); 
    };
    
    recselector.onchange = function()
    {
        recSend = recselector.options[recselector.selectedIndex].value;
        // selectedSend = sendX where X is some number in Jade
        recMP3.src="/audio/" + recSend + ".mp3"; 
        recOGG.src="/audio/" + recSend + ".ogg"; 
        //alert(recMP3.src); 
    };
    
    colorpicker.onchange = function()
    {
        message.style.color = colorpicker.value;
        fontcolor_ = colorpicker.value;
    };
    
    fontselector.onchange = function()
    {
        var selectedFont = fontselector.options[fontselector.selectedIndex].value;
        switch(selectedFont)
        {
            case('arial'):
                message.style.fontFamily = "Arial, Helvetica, sans-serif";
                break;
            case('georgia'):
                message.style.fontFamily = "Georgia, serif";
                break;
            case('palatino'):
                message.style.fontFamily = "'Palatino Linotype', 'Book Antiqua', Palatino, serif";
                break;
            case('timesnewroman'):
                message.style.fontFamily = "'Times New Roman', Times, serif";
                break;
            case('arialblack'):
                message.style.fontFamily = "'Arial Black', Gadget, sans-serif";
                break;
            case('comicsans'):
                message.style.fontFamily = "'Comic Sans MS', cursive, sans-serif";
                break;
            case('impact'):
                message.style.fontFamily = "Impact, Charcoal, sans-serif";
                break;
            case('lucidasans'):
                message.style.fontFamily = "'Lucida Sans Unicode','Lucida Grande', sans-serif";
                break;
            case('tahoma'):
                message.style.fontFamily = "Tahoma, Geneva, sans-serif";
                break;
            case('trebuchet'):
                message.style.fontFamily = "'Trebuchet MS', Helvetica, sans-serif";
                break;
            case('verdana'):
                message.style.fontFamily = "Verdana, Geneva, sans-serif";
                break;
            case('courier'):
                message.style.fontFamily = "'Courier New', Courier, monospace";
                break;
            case('lucidaconsole'):
                message.style.fontFamily = "'Lucida Console', Monaco, monospace";
                break;
            default:
                alert("UNKNOWN FONT TYPE!");
        }
        
        fonttype_ = message.style.fontFamily;
        //alert(fonttype_);
    };
    
    var toggleButton = function(buttonName)
    {
        if(buttonName.style.opacity == 1.0)
        {
            buttonName.style.opacity = 0.3;
            return true; //button is toggled on
        }
        else if(buttonName.style.opacity == 0.3)
        {
            buttonName.style.opacity = 1.0;
            return false; //button is toggled off
        }
        else
        {
            buttonName.style.opacity = 0.3;
            return true; //button is toggled on
        }
    }
    
    italicbutton.onclick = function()
    {
        var toggled = toggleButton(italicbutton);
        if(toggled)
        {
            message.style.fontStyle = 'italic';
            italic_ = true;
        }
        else
        {
            message.style.fontStyle = 'normal';
            italic_ = false;
        }
            
    }
    
    boldbutton.onclick = function()
    {
        var toggled = toggleButton(boldbutton);
        if(toggled)
        {
            message.style.fontWeight = 'bold';
            bold_ = true;
        }
        else
        {
            message.style.fontWeight = 'normal';
            bold_ = false;
        }
    }
    
    var resizeFont = function(factor)
    {
       if(message.style.fontSize == "")
       {
           message.style.fontSize = "15.0px"
       }
        message.style.fontSize = parseFloat(message.style.fontSize) + (factor * 0.75) + "px";
        fontsize_ = message.style.fontSize;
    };
    
    increasefont.onclick = function()
    {
        resizeFont(1);
    };
    
    decreasefont.onclick = function()
    {
        resizeFont(-1);
    };
    
    //check to see if cur group has only one member
    function oneMemberGroup()
    {
        populateMembers();
        return curGroup.members.length === 1;
    }
    
    sendbutton.onclick = function() {
        if(username.value == "") 
            alert("Please type your username!");
        else if(message.value == "") 
            alert("Please type your message!");
        else if(oneMemberGroup())
            alert("Cannot send messages in groups with one member!");
	   else
       {
            // Message send
			//alert("Send sound should be played."); 
			sendsound.load(); 
			sendsound.play();
			//alert(sendMP3.src); 
			//alert(recMP3.src);
            
            history();
            socket.emit('send', { message: message.value, italic: italic_, bold: bold_, fontcolor: fontcolor_, fontsize: fontsize_, fonttype: fonttype_, username: username.value, curgroup: curGroup, recipient: groupmembers.options[selectedMemberIndex].value });
       }
    };
    
    var history = function()
    {
        var num_filter = /^([0-9])*$/;
        var check_num = num_filter.test(msgHistory.value);
        //allow for default
        if(check_num || msgHistory.value == "")
        {
            num_msg = msgHistory.value;
        }
        else
        {
            alert("Please enter a valid number");
        }
    };
}