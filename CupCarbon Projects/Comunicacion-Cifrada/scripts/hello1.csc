atget id id
keysecc priv pub

data helloMessage "hola" id pub
send helloMessage *

loop
	wait 1000
	read m
	cprint m
	rdata m type idN info

	if(type=="hola")
		secretecc idN info priv
    		cipher "saludo" idN saludo
		data message "mensaje" id saludo
		send message idN
	end
	if(type=="mensaje")
    		decipher info idN saludoDec
		cprint "Descifrado"
		cprint saludoDec
	end
