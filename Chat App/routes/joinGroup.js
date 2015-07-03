function joinGrpHand(appio,appsocket,data)
{
    console.log("hopefully joining group: " + data);
    appsocket.join(data);
}

exports.joinGrpHand = joinGrpHand;