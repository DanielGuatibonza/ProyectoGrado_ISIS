atget id id

keysecc priv pub
delay 7200000

data mensajeDetectar "detectarEstacion" id pub
send mensajeDetectar *

set contador 0
updatetime 0

loop
	delay 1000
	set contador contador+1

	read mensajeEntrante
	rdata mensajeEntrante tipo idN params

	if(tipo=="estacionDetectada")
		secretecc idN params priv
		simulatepoint datos
		cipher datos idN mensajeCifrado
		data mensaje "mensaje" id mensajeCifrado
		send mensaje idN
	end
	if(contador==120)
		set contador 0
		updatetime 120
		data mensajeDetectar "detectarEstacion" id pub
		send mensajeDetectar *
	end	