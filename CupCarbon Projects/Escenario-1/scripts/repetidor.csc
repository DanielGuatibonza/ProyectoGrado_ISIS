atget id id
set idEstacion1 -1
set idEstacion2 -1
keysecc priv pub

loop
	delay 1000
	read m
	rdata m tipo idN params
	if(tipo=="registroEstacion")
		secretecc idN params priv
		data mensaje "registroRepetidor" id pub
		send mensaje idN
    		if(idEstacion1==-1)
			set idEstacion1 idN
		else
			set idEstacion2 idN
		end
	end
	if(tipo=="mensajeARepetidor")
    		decipher params idN mensajeDescifrado
		cprint mensajeDescifrado
		if(idEstacion1==idN)
			cipher mensajeDescifrado idEstacion2 mensajeCifrado
			data mensaje "mensaje" id mensajeCifrado
			send mensaje idEstacion2
		else
			cipher mensajeDescifrado idEstacion1 mensajeCifrado
			data mensaje "mensaje" id mensajeCifrado
			send mensaje idEstacion1
		end
	end
