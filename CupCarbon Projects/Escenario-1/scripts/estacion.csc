atget id id
set idRepetidor1 -1
set idRepetidor2 -1
keysecc priv pub

initblockchain

data mensaje "registroEstacion" id pub
send mensaje *

loop
	wait
	read m
	rdata m tipo idN params
	cprint m
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
		savedata mensajeDescifrado
		if(reenviarTransaccion=="true")
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
	end
	if(tipo=="bloqueAValidar")
		decipher params idN contenido
		rdata contenido idEstacionGeneradora bloque
		validateblock bloque idEstacionGeneradora respuestaValidacion
		if(respuestaValidacion!="")
			if(idRepetidor1!=-1)
				cipher respuestaValidacion idRepetidor1 respuestaCifrada
				data mensajeValidacion "bloqueValidoARepetidor" id respuestaCifrada
    				send mensajeValidacion idRepetidor1 
			end
			if(idRepetidor2!=-1)
    				cipher respuestaValidacion idRepetidor2 respuestaCifrada
				data mensajeValidacion "bloqueValidoARepetidor" id respuestaCifrada
    				send mensajeValidacion idRepetidor12
			end
		end
		if(idRepetidor1==idN)
    			cipher contenido idRepetidor2 contenidoCifrado
			data mensaje "bloqueARepetidor" id contenidoCifrado
			send mensaje idRepetidor2
		end
		if(idRepetidor2==idN)
			cipher contenido idRepetidor1 contenidoCifrado
			data mensaje "bloqueARepetidor" id contenidoCifrado
			send mensaje idRepetidor1
		end
	end
	if(tipo=="bloqueValido")
		decipher params idN idEstacion
		if(id==idEstacion)
			savevalidation			
		end
	end
	if(tipo=="timestamp")
		decipher params idN contenido
		rdata contenido idEstacion timestamp
		settimestamp timestamp idEstacion
		if(idRepetidor1==idN)
    			cipher contenido idRepetidor2 contenidoCifrado
			data mensaje "timestampARepetidor" id contenidoCifrado
			send mensaje idRepetidor2
		end
		if(idRepetidor2==idN)
			cipher contenido idRepetidor1 contenidoCifrado
			data mensaje "timestampARepetidor" id contenidoCifrado
			send mensaje idRepetidor1
		end
	end
	if(bloqueNuevo!="")
		data contenido id bloqueNuevo
		if(idRepetidor1!=-1)
    			cipher contenido idRepetidor1 contenidoCifrado
			data mensaje "bloqueARepetidor" id contenidoCifrado
			send mensaje idRepetidor1 
		end
		if(idRepetidor2!=-1)
    			cipher contenido idRepetidor2 contenidoCifrado
			data mensaje "bloqueARepetidor" id contenidoCifrado
			send mensaje idRepetidor2
		end
		set bloqueNuevo ""
	end
	if(timestampUltimo!="")
		data contenido id timestampUltimo
		if(idRepetidor1!=-1)
    			cipher contenido idRepetidor1 timestampCifrado
			data mensaje "timestampARepetidor" id timestampCifrado
			send mensaje idRepetidor1 
		end
		if(idRepetidor2!=-1)
    			cipher contenido idRepetidor2 timestampCifrado
			data mensaje "timestampARepetidor" id timestampCifrado
			send mensaje idRepetidor2 
		end
		set timestampUltimo ""
	end