atget id id
set idRepetidor1 -1
set idRepetidor2 -1
keysecc priv pub

data mensaje "registroEstacion" id pub
send mensaje *

loop
	delay 1000
	read m
	rdata m tipo idN params

	if(tipo=="registroRepetidor")
		secretecc idN params priv
    		if(idRepetidor1==-1)
			set idRepetidor1 idN
		else
			set idRepetidor2 idN
		end
	end
	if(tipo=="detectarEstacion")
		secretecc idN params priv
		data mensaje "estacionDetectada" id pub
		send mensaje idN
	end
	if(tipo=="mensaje")
		decipher params idN mensajeDescifrado
		cprint mensajeDescifrado
		if(idRepetidor1!=-1)
    			cipher mensajeDescifrado idRepetidor1 mensajeCifrado
			data mensaje "mensajeARepetidor" id mensajeCifrado
			send mensaje idRepetidor1 
		end
		if(idRepetidor2!=-1)
    			cipher mensajeDescifrado idRepetidor2 mensajeCifrado
			data mensaje "mensajeARepetidor" id mensajeCifrado
			send mensaje idRepetidor2 
		end
	end