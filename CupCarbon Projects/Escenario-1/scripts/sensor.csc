atget id id
keysecc priv pub

set contador 1

loop
	delay 10000
	read m
	rdata m tipo idN params

	data mensajeDetectar "detectarEstacion" id pub
	send mensajeDetectar *

	read mensajeEntrante
	rdata mensajeEntrante tipo idN params

	if(tipo=="estacionDetectada")
		secretecc idN params priv
		cipher contador idN mensajeCifrado
		data mensaje "mensaje" id mensajeCifrado
		send mensaje idN
		set contador contador+1
	end