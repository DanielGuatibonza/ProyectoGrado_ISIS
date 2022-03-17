import socket, threading, haversine
import pandas as pd

lat_long_df = pd.read_excel('./Longitud_Latitud.xlsx')

MANEJADOR_IP = '192.158.0.201'
PUERTO = '8081'
CONEXION = 'C'
DESCONEXION = 'D'

host = socket.gethostname()
ip_local = socket.gethostbyname(host)

socket_principal = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket_principal.connect((MANEJADOR_IP, PUERTO))

socket_principal.sendall(CONEXION.encode())
socket_principal.sendall(('0;{:.13f};{:.13f}'.format(lat_long_df.iloc[0,0], lat_long_df.iloc[0,1])).encode())
