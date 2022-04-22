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
	if(tipo=="bloqueARepetidor")
		decipher params idN contenidoDescifrado
		if(idEstacion1==idN)
			cipher contenidoDescifrado idEstacion2 mensajeCifrado
			data mensaje "bloqueAValidar" id mensajeCifrado
			send mensaje idEstacion2
		else
			cipher contenidoDescifrado idEstacion1 mensajeCifrado
			data mensaje "bloqueAValidar" id mensajeCifrado
			send mensaje idEstacion1
		end
	end
	if(tipo=="bloqueValidoARepetidor")
		decipher params idN contenidoDescifrado
		if(idEstacion1==idN)
			cipher contenidoDescifrado idEstacion2 mensajeCifrado
			data mensaje "bloqueValido" id mensajeCifrado
			send mensaje idEstacion2
		else
			cipher contenidoDescifrado idEstacion1 mensajeCifrado
			data mensaje "bloqueValido" id mensajeCifrado
			send mensaje idEstacion1
		end
	end
	if(tipo=="timestampARepetidor")
		decipher params idN contenidoDescifrado
		if(idEstacion1==idN)
			cipher contenidoDescifrado idEstacion2 mensajeCifrado
			data mensaje "timestamp" id mensajeCifrado
			send mensaje idEstacion2
		else
			cipher contenidoDescifrado idEstacion1 mensajeCifrado
			data mensaje "timestamp" id mensajeCifrado
			send mensaje idEstacion1
		end
	end