atget id id
set idRepetidor1 -1
set idRepetidor2 -1
set reenviarTransaccion "false"
keysecc priv pub

initblockchain

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
			cprint idRepetidor1
		else
			if(idRepetidor2==-1)
				set idRepetidor2 idN
				cprint idRepetidor2
			end
		end
	end
	if(tipo=="detectarEstacion")
		secretecc idN params priv
		data mensaje "estacionDetectada" id pub
		send mensaje idN
	end
	if(tipo=="mensaje")
		decipher params idN mensajeDescifrado
		savedata mensajeDescifrado
		if(reenviarTransaccion=="true")
			if(idRepetidor1==idN)
				if(idRepetidor2!=-1)
    					cipher mensajeDescifrado idRepetidor2 mensajeCifrado
					data mensaje "mensajeARepetidor" id mensajeCifrado
					send mensaje idRepetidor2
				end
			end
			if(idRepetidor2==idN)
				if(idRepetidor1!=-1)
    					cipher mensajeDescifrado idRepetidor1 mensajeCifrado
					data mensaje "mensajeARepetidor" id mensajeCifrado
					send mensaje idRepetidor1
				end
			end
		end
	end
	if(tipo=="bloqueAValidar")
		decipher params idN contenido
		rdata contenido idEstacionGeneradora bloque
		if(idRepetidor1==idN)
			if(idRepetidor2!=-1)
    				cipher contenido idRepetidor2 contenidoCifrado
				data mensaje "bloqueARepetidor" id contenidoCifrado
				send mensaje idRepetidor2
			end
		end
		if(idRepetidor2==idN)
			if(idRepetidor1!=-1)
				cipher contenido idRepetidor1 contenidoCifrado
				data mensaje "bloqueARepetidor" id contenidoCifrado
				send mensaje idRepetidor1
			end
		end
		validateblock bloque idEstacionGeneradora respuestaValidacion
		if(respuestaValidacion!="invalido")
			if(idRepetidor1==idN)
				cipher respuestaValidacion idRepetidor1 respuestaCifrada
				data mensajeValidacion "bloqueValidoARepetidor" id respuestaCifrada
    				send mensajeValidacion idRepetidor1 
			end
			if(idRepetidor2==idN)
    				cipher respuestaValidacion idRepetidor2 respuestaCifrada
				data mensajeValidacion "bloqueValidoARepetidor" id respuestaCifrada
    				send mensajeValidacion idRepetidor2
			end
		end

	end
	if(tipo=="bloqueValido")
		decipher params idN idEstacion
		savevalidation idEstacion
		if(idN==idRepetidor2)
			if(idRepetidor1!=-1)
				cipher idEstacion idRepetidor1 respuestaCifrada
				data mensajeValidacion "bloqueValidoARepetidor" id respuestaCifrada
    				send mensajeValidacion idRepetidor1 
			end
		end
		if(idN==idRepetidor1)
			if(idRepetidor2!=-1)
    				cipher idEstacion idRepetidor2 respuestaCifrada
				data mensajeValidacion "bloqueValidoARepetidor" id respuestaCifrada
    				send mensajeValidacion idRepetidor2
			end
		end
	end
	if(tipo=="timestamp")
		decipher params idN contenido
		rdata contenido timestamp hashUltimo
		settimestamp timestamp hashUltimo
		if(idRepetidor1==idN)
			if(idRepetidor2!=-1)
    				cipher contenido idRepetidor2 contenidoCifrado
				data mensaje "timestampARepetidor" id contenidoCifrado
				send mensaje idRepetidor2
			end
		end
		if(idRepetidor2==idN)
			if(idRepetidor1!=-1)
				cipher contenido idRepetidor1 contenidoCifrado
				data mensaje "timestampARepetidor" id contenidoCifrado
				send mensaje idRepetidor1
			end
		end
	end
	if(tipo=="rutaModelo")
		decipher params idN contenido
		rdata contenido idEstacion rutaModelo
		saveroute idEstacion rutaModelo
		if(idRepetidor1==idN)
			if(idRepetidor2!=-1)
    				cipher contenido idRepetidor2 contenidoCifrado
				data mensaje "rutaModeloARepetidor" id contenidoCifrado
				send mensaje idRepetidor2
			end
		end
		if(idRepetidor2==idN)
			if(idRepetidor1!=-1)
				cipher contenido idRepetidor1 contenidoCifrado
				data mensaje "rutaModeloARepetidor" id contenidoCifrado
				send mensaje idRepetidor1
			end
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
		data contenido timestampUltimo hashUltimo
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
	if(rutaModelo!="")
		data contenido id rutaModelo
		if(idRepetidor1!=-1)
    			cipher contenido idRepetidor1 contenidoCifrado
			data mensaje "rutaModeloARepetidor" id contenidoCifrado
			send mensaje idRepetidor1 
		end
		if(idRepetidor2!=-1)
    			cipher contenido idRepetidor2 contenidoCifrado
			data mensaje "rutaModeloARepetidor" id contenidoCifrado
			send mensaje idRepetidor2 
		end
		set rutaModelo ""
	end