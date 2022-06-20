from datetime import datetime
import json

ruta = './Dificultad {}/blockchain-{}.json'
# ruta = './PoLe/blockchain-{}.json'
patron = '%Y-%m-%d %H:%M:%S'

num_nodos = 8
dificultad = 7
blockchain_mas_grande = json.load(open(ruta.format(dificultad, 1)))
for nodo in range(2, num_nodos):
    blockchain_actual = json.load(open(ruta.format(dificultad, nodo)))
    if len(blockchain_actual) > len(blockchain_mas_grande):
        blockchain_mas_grande = blockchain_actual
fecha_bloque_anterior = datetime.strptime(blockchain_mas_grande[0]['timestamp'], patron)
for i in range(1, len(blockchain_mas_grande)):
    fecha_bloque_actual = datetime.strptime(blockchain_mas_grande[i]['timestamp'], patron)
    print((fecha_bloque_actual - fecha_bloque_anterior).seconds)
    fecha_bloque_anterior = fecha_bloque_actual

        