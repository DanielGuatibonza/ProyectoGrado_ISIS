from datetime import datetime
import json

num_dificultades = 7
num_nodos = 8
dificultad = 7
blockchain_mas_grande = json.load(open('./Dificultad {}/blockchain-{}.json'.format(dificultad, 1)))
for nodo in range(2, num_nodos):
    blockchain_actual = json.load(open('./Dificultad {}/blockchain-{}.json'.format(dificultad, nodo)))
    if len(blockchain_actual) > len(blockchain_mas_grande):
        blockchain_mas_grande = blockchain_actual
fecha_bloque_anterior = datetime.strptime(blockchain_mas_grande[0]['timestamp'], '%Y-%m-%d %H:%M:%S')
for i in range(1, len(blockchain_mas_grande)):
    fecha_bloque_actual = datetime.strptime(blockchain_mas_grande[i]['timestamp'], '%Y-%m-%d %H:%M:%S')
    print((fecha_bloque_actual - fecha_bloque_anterior).seconds)
    fecha_bloque_anterior = fecha_bloque_actual

        