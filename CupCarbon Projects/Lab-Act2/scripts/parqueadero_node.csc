atget id id
set cupos id

loop
	read mens
	rdata mens tipo Bid
	if(tipo=="cupos")
		data mens "cupos" id cupos
		send mens Bid
	end
	wait 100