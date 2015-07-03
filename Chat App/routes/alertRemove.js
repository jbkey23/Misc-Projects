function alertRm(appio,appsocket,data)
{
	appsocket.broadcast.to(data.userid).emit('removeAlert');
}

exports.alertRm = alertRm;