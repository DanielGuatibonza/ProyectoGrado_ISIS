atget id id
set x 0
set i 0
set tipo ""
loop
	atnd n
	read mens
	rdata mens tipo valor
	if((tipo=="hola") && (x==0))
		set x 1
		data mens "parqueo" id
		send mens 4
	else
		if((n>0) && (x==0))
			data mens "hola" id
			send mens 4
		end
	end
	if(tipo=="parqueo")
		route valor
	end
	wait 100