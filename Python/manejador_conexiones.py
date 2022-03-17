import socket, threading, haversine

IP_DESCONEXION = 15 
TIPO_CONEXION = 1 + 1 + 15 + 1 + 17  # 1 IoT/Nodo + ; + 15 Lat + ; + 17 Lon
CONEXION_DESCONEXION = 1  # C=Conectar, D=Desconectar
MAX_NODOS_MULTICONEXION = 8

host = socket.gethostname()
ip_local = socket.gethostbyname(host)
puerto = '8081'
dict_nodos, dict_iots = {}, {}

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((ip_local, puerto))
s.listen()
print('Escuchando en', s.getsockname())

def recibir_mensaje(sock, largo):
    datos = ''
    while len(datos) < largo:
        paquete = sock.recv(largo - len(datos))
        datos_actual = paquete.decode()
        if not datos_actual:
            raise EOFError('Socket cerrado.')
        datos += datos_actual
    return datos

def manejar_conexion(sc, sockname):
    global lista_nodos, lista_iots
    print('El socket se conecta desde', sc.getsockname(), 'hacia', sc.getpeername())
    objetivo_comunicacion = recibir_mensaje(sc, CONEXION_DESCONEXION)
    if objetivo_comunicacion == 'C':
        conectar(sc, sockname)
    elif objetivo_comunicacion == 'D':
        desconectar(sc, sockname)

def conectar(sc, sockname):
    def calcular_distancias(lat, lon, dict_a_conectar):
        lista = [(sockname[0], 
                  len(dict_datos['Conexiones'], 
                  haversine((lat,lon),(dict_datos['Latitud'], dict_datos['Longitud'])))) 
                  for sockname, dict_datos in dict_a_conectar.items()]
        return sorted(lista, key=lambda x: (x[1], x[2]))
    mensaje = recibir_mensaje(sc, TIPO_CONEXION)
    partes_mensaje = mensaje.split(';')
    lat, lon = float(partes_mensaje[1]), float(partes_mensaje[2])

    if partes_mensaje[0] == '0': #Iot
        dict_iots[sockname[0]] = {'Latitud': lat, 'Longitud': lon, 'Conexiones': []}
        if len(dict_nodos) > 0: # Conectar IoT a nodos
            lista = calcular_distancias(lat, lon, dict_nodos)
            num_conexiones = (MAX_NODOS_MULTICONEXION // len(dict_nodos)) + 2
            for i in range(num_conexiones):
                if i >= len(lista):
                    break
                dict_iots[sockname[0]]['Conexiones'].append([lista[i][0], True])
                dict_nodos[lista[i][0]]['Conexiones'].append([sockname[0], True])

    else: #Nodo
        dict_nodos[sockname[0]] = {'Latitud': lat, 'Longitud': lon, 'Conexiones': []}
        if len(dict_nodos) > 0: # Conectar nodos entre si
            lista = calcular_distancias(lat, lon, dict_nodos)
            num_conexiones = (MAX_NODOS_MULTICONEXION // len(dict_nodos)) + 2
            for i in range(num_conexiones):
                if i >= len(lista):
                    break
                dict_nodos[sockname[0]]['Conexiones'].append([lista[i][0], True])
                dict_nodos[lista[i][0]]['Conexiones'].append([sockname[0], True])
        if len(dict_iots) > 0: # Conectar nodo a IoT
            lista = calcular_distancias(lat, lon, dict_iots)
            num_conexiones = (MAX_NODOS_MULTICONEXION // len(dict_iots)) + 2
            for i in range(num_conexiones):
                if i >= len(lista):
                    break
                dict_nodos[sockname[0]]['Conexiones'].append([lista[i][0], True])
                dict_iots[lista[i][0]]['Conexiones'].append([sockname[0], True])

def desconectar(sc, sockname):

    def eliminar(ip, dict_datos):
        del dict_datos[sockname[0]]
        for ip_actual, dict_actual in dict_datos.items():
            if ip in dict_actual['Conexiones']:
                dict_datos[ip_actual]['Conexiones'].remove(ip)
        return dict_datos

    ip_desconectar = recibir_mensaje(sc, IP_DESCONEXION)
    es_nodo = ip_desconectar in dict_nodos.keys()
    
    if ip_desconectar == sockname[0]:
        dict_nodos = eliminar(ip_desconectar, dict_nodos)
        if es_nodo:
            dict_iots = eliminar(ip_desconectar, dict_iots)

    elif es_nodo and sockname[0] in dict_nodos[ip_desconectar]['Conexiones']:
        indice = -1
        for i, sub_list in enumerate(dict_nodos[ip_desconectar]['Conexiones']):
            if sub_list[0] == sockname[0]:
                indice = i
                break
        dict_nodos[ip_desconectar]['Conexiones'][indice][1] = False
        num_false = sum([1 for conexion in dict_nodos[ip_desconectar]['Conexiones'] if conexion[1] == False])
        if num_false > 0.5*len(dict_nodos[ip_desconectar]['Conexiones']):
            dict_nodos = eliminar(ip_desconectar, dict_nodos)
            dict_iots = eliminar(ip_desconectar, dict_iots)
        
    elif not es_nodo and sockname[0] in dict_iots[ip_desconectar]['Conexiones']:
        indice = -1
        for i, sub_list in enumerate(dict_iots[ip_desconectar]['Conexiones']):
            if sub_list[0] == sockname[0]:
                indice = i
                break
        dict_iots[ip_desconectar]['Conexiones'][indice][1] = False
        num_false = sum([1 for conexion in dict_nodos[ip_desconectar]['Conexiones'] if conexion[1] == False])
        if num_false > 0.5*len(dict_nodos[ip_desconectar]['Conexiones']):
            dict_nodos = eliminar(ip_desconectar, dict_nodos)

try:
    while True:
        sc, sockname = s.accept()
        print('-------------------------------------------------')
        print('Se ha aceptado una conexión de', str(sockname))
        th = threading.Thread(target=manejar_conexion, args=(sc, sockname))
        th.start()
except KeyboardInterrupt:
    print('Cerrando el servidor')
finally:
    s.close()